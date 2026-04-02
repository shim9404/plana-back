package com.example.plana.service;

import com.example.plana.dto.trip.create.*;
import com.example.plana.dto.trip.update.*;
import com.example.plana.mapper.TripMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class TripService {
    private final TripMapper tripMapper;

    /**
     * 신규 여행 생성 - 날짜 수만큼 신규 여행 일자 생성 - 각 여행 일자당 1개의 신규 스케줄 생성
     * // @Transactional : 여행 - 여행 일자 - 여행 스케줄 생성을 하나의 트랜잭션으로
     * @param request TripRequest
     * @return TripResponse 여행 일자와 여행 스케줄이 포함된 신규 생성 여행 데이터 반환
     */
    @Transactional
    public TripCreateResponse createTrip(TripCreateRequest request) {

        // 1. TRIP INSERT
        Map<String, Object> tripParams = new HashMap<>();
        tripParams.put("memberId", request.getMemberId());
        tripParams.put("name", checkName(request.getName(), "나의 새로운 여행"));
        tripParams.put("startDate", checkDate(request.getStartDate()));
        tripParams.put("endDate", checkDate(request.getEndDate()));
        tripParams.put("tripId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripId의 반환값을 담아야 함

        tripMapper.createTrip(tripParams);
        String tripId = (String) tripParams.get("tripId");

        // 2. 날짜 범위 계산
        LocalDate startDate = parseDate(tripParams.get("startDate").toString());
        LocalDate endDate   = parseDate(tripParams.get("endDate").toString());
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // 3. TRIP_DAY + TRIP_SCHEDULE INSERT
        List<TripDayCreateResponse> dayList = new ArrayList<>();

        for (int i = 1; i <= days; i++) {

            // TRIP_DAY INSERT
            Map<String, Object> dayParams = new HashMap<>();
            dayParams.put("tripId", tripId);
            dayParams.put("indexSort", i);
            dayParams.put("tripDayId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripDayId의 반환값을 담아야 함

            tripMapper.createTripDay(dayParams);
            String tripDayId = (String) dayParams.get("tripDayId");

            // TRIP_SCHEDULE INSERT
            Map<String, Object> scheduleParams = new HashMap<>();
            scheduleParams.put("tripDayId", tripDayId);
            scheduleParams.put("tripScheduleId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripScheduleId의 반환값을 담아야 함

            tripMapper.createTripSchedule(scheduleParams);
            String tripScheduleId = (String) scheduleParams.get("tripScheduleId");

            // 일자와 스케줄의 Response 데이터에 담아 조립
            TripScheduleCreateResponse schedule = TripScheduleCreateResponse.builder()
                    .tripScheduleId(tripScheduleId)
                    .indexSort(1)
                    .build();

            TripDayCreateResponse day = TripDayCreateResponse.builder()
                    .tripDayId(tripDayId)
                    .indexSort(i)
                    .schedules(List.of(schedule))
                    .build();

            dayList.add(day);
        }

        // 4. 최종 응답 반환
        return TripCreateResponse.builder()
                .tripId(tripId)
                .name((String) tripParams.get("name"))
                .startDate((String) tripParams.get("startDate"))
                .endDate((String) tripParams.get("endDate"))
                .bookmarks(Collections.emptyList())
                .days(dayList)
                .build();
    }

    /**
     * 이름 유효성 체크
     * @param name String 입력한 이름
     * @param defaultName String 입력한 이름이 비었을 경우, 기본으로 설정할 이름
     * @return String 입력한 여행명 (없을 경우 "나의 새로운 여행"으로 반환)
     */
    private String checkName(String name, String defaultName) {
        return (name != null && !name.isBlank()) ? name : defaultName;
    }

    /**
     * 날짜 유효성 체크
     * @param date String 입력한 날짜
     * @return String 입력한 날짜 (없을 경우 현재 시간 반환)
     */
    private String checkDate(String date) {
        return (date != null && !date.isBlank())
                ? date
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 날짜 텍스트 DateTime Format으로 파싱하여 반환
     * @param date 날짜 문자열
     * @return LocalDate 날짜
     */
    private LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 여행 전체 저장
     * 작동 시나리오 : 네트워크 에러 등의 사유로 편집 시 자동 저장이 이루어지지 않았을 경우, 저장 시점에 기존 데이터를 전부 삭제한 뒤 재생성한다.
     * // @Transactional : 여행 정보 갱신 - 여행 일자 및 스케줄 삭제(delete) - 여행 일자 및 스케줄 재생성(insert)을 하나의 트랜잭션으로
     * @param tripId String
     * @param request TripUpdateRequest
     * @return TripUpdateResponse
     */
    @Transactional
    public TripUpdateResponse saveTrip(String tripId, TripUpdateRequest request) {
        // 1. TRIP UPDATE
        Map<String, Object> tripParams = new HashMap<>();
        tripParams.put("tripId",    tripId);
        tripParams.put("name",      checkName(request.getName(), "[네트워크 에러] 복구된 여행 계획"));
        tripParams.put("startDate", checkDate(request.getStartDate()));
        tripParams.put("endDate",   checkDate(request.getEndDate()));

        log.info(request);
        tripMapper.updateTrip(tripParams);


        // 2. 기존 하위 데이터 전체 삭제 (자식 먼저) : DELETE 사용
        tripMapper.deleteTripSchedulesByTripId(tripId);
        tripMapper.deleteTripDaysByTripId(tripId);


        // 3. TRIP_DAY + TRIP_SCHEDULE 재삽입 : CREATE 사용
        List<TripDayUpdateResponse> dayList = new ArrayList<>();

        for (TripDayUpdateRequest dayRequest : request.getDays()) {

            Map<String, Object> dayParams = new HashMap<>();
            dayParams.put("tripId",    tripId);
            dayParams.put("indexSort", dayRequest.getIndexSort());
            dayParams.put("tripDayId", null);

            tripMapper.createTripDay(dayParams);
            String tripDayId = (String) dayParams.get("tripDayId");

            List<TripScheduleUpdateResponse> scheduleList = new ArrayList<>();

            for (TripScheduleUpdateRequest scheduleRequest
                    : dayRequest.getSchedules()) {

                Map<String, Object> scheduleParams = new HashMap<>();
                scheduleParams.put("tripDayId",      tripDayId);
                scheduleParams.put("indexSort",      scheduleRequest.getIndexSort());
                scheduleParams.put("startTime",      scheduleRequest.getStartTime());
                scheduleParams.put("endTime",        scheduleRequest.getEndTime());
                scheduleParams.put("context",        scheduleRequest.getContext());
                scheduleParams.put("category",       scheduleRequest.getCategory());
                scheduleParams.put("price",          scheduleRequest.getPrice());
                scheduleParams.put("memo",           scheduleRequest.getMemo());
                scheduleParams.put("link",           scheduleRequest.getLink());
                scheduleParams.put("tripScheduleId", null);

                tripMapper.createTripSchedules(scheduleParams);
                String tripScheduleId = (String) scheduleParams.get("tripScheduleId");

                scheduleList.add(TripScheduleUpdateResponse.builder()
                        .tripScheduleId(tripScheduleId)
                        .tripDayId(tripDayId)
                        .indexSort(scheduleRequest.getIndexSort())
                        .startTime(scheduleRequest.getStartTime())
                        .endTime(scheduleRequest.getEndTime())
                        .context(scheduleRequest.getContext())
                        .category(scheduleRequest.getCategory())
                        .price(scheduleRequest.getPrice())
                        .memo(scheduleRequest.getMemo())
                        .link(scheduleRequest.getLink())
                        .build());
            }

            dayList.add(TripDayUpdateResponse.builder()
                    .tripDayId(tripDayId)
                    .indexSort(dayRequest.getIndexSort())
                    .schedules(scheduleList)
                    .build());
        }

        return TripUpdateResponse.builder()
                .tripId(tripId)
                .name((String) tripParams.get("name"))
                .startDate((String) tripParams.get("startDate"))
                .endDate((String) tripParams.get("endDate"))
                .bookmarks(Collections.emptyList())
                .days(dayList)
                .build();
    }
}
