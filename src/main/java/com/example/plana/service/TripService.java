package com.example.plana.service;

import com.example.plana.dto.trip.create.TripCreateRequest;
import com.example.plana.dto.trip.create.TripCreateResponse;
import com.example.plana.dto.trip.create.TripDayCreateResponse;
import com.example.plana.dto.trip.create.TripScheduleCreateResponse;
import com.example.plana.dto.trip.update.TripScheduleUpdateRequest;
import com.example.plana.dto.trip.update.TripUpdateRequest;
import com.example.plana.mapper.TripMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
        tripParams.put("name", checkName(request.getName()));
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
     * 여행명 유효성 체크
     * @param name String 입력한 여행명
     * @return String 입력한 여행명 (없을 경우 "나의 새로운 여행"으로 반환)
     */
    private String checkName(String name) {
        return (name != null && !name.isBlank()) ? name : "나의 새로운 여행";
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

    @Transactional
    public void saveTrip(String tripId, TripUpdateRequest request) {
        request.setTripId(tripId);
        tripMapper.updateTrip(request);


        // 2. TRIP_DAY MERGE (추가/수정/삭제 한 번에)
        if (!request.getDays().isEmpty()) {
            tripMapper.updateTripDays(request);
        }
    }
}
