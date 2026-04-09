package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.common.utils.DateUtils;
import com.example.plana.dto.trip.create.*;
import com.example.plana.dto.trip.read.*;
import com.example.plana.dto.trip.update.*;
import com.example.plana.mapper.BookmarkMapper;
import com.example.plana.mapper.TripMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class TripService {
    private final TripMapper tripMapper;
    private final BookmarkMapper bookmarkMapper;

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
        tripParams.put("startDate", request.getStartDate());
        tripParams.put("endDate", request.getEndDate());
        tripParams.put("tripId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripId의 반환값을 담아야 함

        try {
            tripMapper.createTrip(tripParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_CREATE_FAILED);
        }
        String tripId = (String) tripParams.get("tripId");

        // 2. 날짜 범위 계산
        LocalDate startDate = DateUtils.parseDate(tripParams.get("startDate").toString());
        LocalDate endDate   = DateUtils.parseDate(tripParams.get("endDate").toString());
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // 3. TRIP_DAY + TRIP_SCHEDULE INSERT
        List<TripDayCreateResponse> dayList = new ArrayList<>();

        for (int i = 1; i <= days; i++) {

            // TRIP_DAY INSERT
            Map<String, Object> dayParams = new HashMap<>();
            dayParams.put("tripId", tripId);
            dayParams.put("indexSort", i);
            dayParams.put("tripDayId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripDayId의 반환값을 담아야 함

            try {
                tripMapper.createTripDay(dayParams);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.TRIP_DAY_CREATE_FAILED);
            }
            String tripDayId = (String) dayParams.get("tripDayId");

            // TRIP_SCHEDULE INSERT
            Map<String, Object> scheduleParams = new HashMap<>();
            scheduleParams.put("tripDayId", tripDayId);
            scheduleParams.put("indexSort", 1);
            scheduleParams.put("startTime", null);
            scheduleParams.put("endTime", null);
            scheduleParams.put("bookmarkId", null);
            scheduleParams.put("context", null);
            scheduleParams.put("category", null);
            scheduleParams.put("price", null);
            scheduleParams.put("memo", null);
            scheduleParams.put("link", null);
            scheduleParams.put("tripScheduleId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripScheduleId의 반환값을 담아야 함
            try {
                tripMapper.createTripSchedule(scheduleParams);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.TRIP_SCHEDULE_CREATE_FAILED);
            }

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
     * 여행 단건 상세 조회
     * @param tripId 여행 ID
     * @return TripResponse
     */
    @Transactional
    public TripResponse readTrip(String tripId) {
        // 1. SELECT TRIP
        TripResponse trip = null;
        try {
            trip = tripMapper.readTrip(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_READ_FAILED);
        }
        log.info(trip);
        if (trip == null) throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);

        // 2. SELECT TRIP DAYS
        try {
            // TRIP_ID에 해당하는 모든 TRIP_DAY 리스트에 담기
            List<TripDayResponse> days = tripMapper.readTripDaysByTripId(tripId);
            for (TripDayResponse day : days) {
                log.info(day.getTripDayId());
                // 3. SELECT TRIP SCHEDULES
                try {
                    // 각 TRIP_DAY_ID에 해당하는 모든 TRIP_SCHEDULE 리스트에 담기
                    List<TripScheduleResponse> schedules = tripMapper.readTripSchedulesByTripDayId(day.getTripDayId());
                    day.setSchedules(schedules);
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.TRIP_SCHEDULE_READ_FAILED);
                }
            }
            trip.setDays(days);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_READ_FAILED);
        }

        return trip;
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
        tripParams.put("startDate", DateUtils.checkDate(request.getStartDate()));
        tripParams.put("endDate",   DateUtils.checkDate(request.getEndDate()));
        log.info(request);

        int result = -1;
        try {
            result = tripMapper.updateTrip(tripParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_UPDATE_FAILED);
        }

        if (result == 0) {      // 업데이트 결과에 따른 예외 처리
            throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);
        }

        // 2. 기존 하위 데이터 전체 삭제 (자식 먼저) : DELETE 사용
        try {
            tripMapper.deleteTripSchedulesByTripId(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_DELETE_FAILED);
        }
        try {
            tripMapper.deleteTripDaysByTripId(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_DELETE_FAILED);
        }

        // 3. TRIP_DAY + TRIP_SCHEDULE 재삽입 : CREATE 사용
        List<TripDayUpdateResponse> dayList = new ArrayList<>();

        for (TripDayUpdateRequest dayRequest : request.getDays()) {

            Map<String, Object> dayParams = new HashMap<>();
            dayParams.put("tripId",    tripId);
            dayParams.put("indexSort", dayRequest.getIndexSort());
            dayParams.put("tripDayId", null);

            try {
                tripMapper.createTripDay(dayParams);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.TRIP_DAY_CREATE_FAILED);
            }
            String tripDayId = (String) dayParams.get("tripDayId");

            List<TripScheduleUpdateResponse> scheduleList = new ArrayList<>();

            for (TripScheduleUpdateRequest scheduleRequest
                    : dayRequest.getSchedules()) {

                Map<String, Object> scheduleParams = new HashMap<>();
                scheduleParams.put("tripDayId",     tripDayId);
                scheduleParams.put("indexSort",     scheduleRequest.getIndexSort());
                scheduleParams.put("startTime",     scheduleRequest.getStartTime());
                scheduleParams.put("endTime",       scheduleRequest.getEndTime());
                scheduleParams.put("bookmarkId",    scheduleRequest.getBookmarkId());
                scheduleParams.put("context",       scheduleRequest.getContext());
                scheduleParams.put("category",      scheduleRequest.getCategory());
                scheduleParams.put("price",         scheduleRequest.getPrice());
                scheduleParams.put("memo",          scheduleRequest.getMemo());
                scheduleParams.put("link",          scheduleRequest.getLink());
                scheduleParams.put("tripScheduleId", null);

                try {
                    tripMapper.createTripSchedule(scheduleParams);
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.TRIP_SCHEDULE_DELETE_FAILED);
                }
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

    /**
     * 여행 정보(시작 일자, 종료 일자, 여행명) 갱신
     * @param tripId 여행 ID
     * @param request TripInfoUpdateRequest
     */
    public void updateTripInfo(String tripId, TripInfoUpdateRequest request) {
        Map<String, Object> tripParams = new HashMap<>();
        tripParams.put("tripId",    tripId);
        tripParams.put("startDate", request.getStartDate());
        tripParams.put("endDate",   request.getEndDate());
        tripParams.put("name",   request.getName());
        log.info(request);

        int result = -1;
        try {
            result = tripMapper.updateTrip(tripParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_UPDATE_FAILED);
        }
        if (result == 0) {      // 업데이트 결과에 따른 예외 처리
            throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);
        }
    }

    /**
     * 여행 상태(Status) 갱신
     * @param tripId 여행 ID
     * @param request TripStatusUpdateResponse : STATUS - ACTIVE(활성) / INACTIVE(비활성) / DELETED(삭제)
     */
    public void updateTripStatus(String tripId, TripStatusUpdateRequest request) {
        Map<String, Object> tripParams = new HashMap<>();
        tripParams.put("tripId",    tripId);
        tripParams.put("status", request.getStatus());
        int result = -1;
        try {
            result = tripMapper.updateTripStatus(tripParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_UPDATE_FAILED);
        }
        if (result == 0) {
            throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);
        }
    }

    /**
     * 여행 단건 삭제 - 하위 일자 및 스케줄 전체 삭제
     * @param tripId 여행 ID
     */
    @Transactional
    public void deleteTrip(String tripId) {
        // 1. 여행 하위 데이터 전체 삭제 DELETE
        try {   // 스케줄 우선 삭제
            tripMapper.deleteTripSchedulesByTripId(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_DELETE_FAILED);
        }
        try {   // 일자 삭제
            tripMapper.deleteTripDaysByTripId(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_DELETE_FAILED);
        }
        try {   // 북마크 삭제
            bookmarkMapper.deleteBookmarksByTripId(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_BOOKMARK_DELETE_FAILED);
        }

        // 2. 여행 삭제
        int result = -1;
        try {
            result = tripMapper.deleteTrip(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DELETE_FAILED);
        }
        if (result == 0) {
            throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);
        }
    }

    /* 여행 일자 [TRIP_DAY] ============================================================================================*/
    /**
     * 여행 일자 신규 추가
     * @param tripId 여행 ID
     * @param request TripDayCreateRequest
     * @return TripDayCreateResponse
     */
    @Transactional
    public TripDayCreateResponse createTripDay(String tripId, TripDayCreateRequest request) {
        // 1. TRIP_DAY INSERT (단건)
        Map<String, Object> dayParams = new HashMap<>();
        dayParams.put("tripId", tripId);
        dayParams.put("indexSort", request.getIndexSort());
        dayParams.put("tripDayId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripDayId의 반환값을 담아야 함

        try {
            tripMapper.createTripDay(dayParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_CREATE_FAILED);
        }
        String tripDayId = (String) dayParams.get("tripDayId");

        // 2. TRIP_SCHEDULE INSERT (단건)
        Map<String, Object> scheduleParams = new HashMap<>();
        scheduleParams.put("tripDayId", tripDayId);
        scheduleParams.put("indexSort", 1);
        scheduleParams.put("startTime", null);
        scheduleParams.put("endTime", null);
        scheduleParams.put("bookmarkId", null);
        scheduleParams.put("context", null);
        scheduleParams.put("category", null);
        scheduleParams.put("price", null);
        scheduleParams.put("memo", null);
        scheduleParams.put("link", null);
        scheduleParams.put("tripScheduleId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripScheduleId의 반환값을 담아야 함

        try {
            tripMapper.createTripSchedule(scheduleParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_CREATE_FAILED);
        }
        String tripScheduleId = (String) scheduleParams.get("tripScheduleId");

        // 3. 일자와 스케줄의 Response 데이터에 담아 조립
        TripScheduleCreateResponse schedule = TripScheduleCreateResponse.builder()
                .tripScheduleId(tripScheduleId)
                .indexSort(1)
                .build();

        // 4. 최종 응답 반환
        return TripDayCreateResponse.builder()
                .tripDayId(tripDayId)
                .indexSort(request.getIndexSort())
                .schedules(List.of(schedule))
                .build();
    }

    /**
     * 여행 일자 삭제(여행 일자 하위 스케줄 포함)
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     */
    @Transactional
    public void deleteTripDay(String tripId, String tripDayId) {
        Map<String, Object> deleteParams = new HashMap<>();
        deleteParams.put("tripId", tripId);
        deleteParams.put("tripDayId", tripDayId);
        // 1. SELECT TRIP DAY - getIndexSort
        int indexSort = -1;
        indexSort = tripMapper.readTripDayIndexSort(tripDayId);
        if (indexSort <= 0) throw new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND);

        // 2. 여행 스케줄 전체 삭제
        int result = -1;
        try {
            result = tripMapper.deleteTripSchedulesByTripDayId(deleteParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_DELETE_FAILED);
        }
        if (result == 0) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
        }

        // 3. 여행 일자 삭제
        result = -1;
        try {
            result = tripMapper.deleteTripDay(deleteParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_DELETE_FAILED);
        }
        if (result == 0) {
            throw new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND);
        }

        // 4. 삭제된 일자 이후의 index sort 재정렬
        Map<String, Object> reorderParams = new HashMap<>();
        reorderParams.put("tripId", tripId);
        reorderParams.put("indexSort", indexSort);
        try {
            tripMapper.updateTripDaysIndexSortAfterDelete(reorderParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_REORDER_FAILED);
        }
    }

    /**
     * 여행 일자 전체 재정렬
     * @param tripId 여행 ID
     * @param request TripDayOrderUpdateRequest
     */
    @Transactional
    public void updateTripDaysIndexSort(String tripId, TripDayOrderUpdateRequest request) {
        for (TripDayOrderUpdateRequest.DayOrder dayOrder : request.getDayOrders()) {
            Map<String, Object> dayParams = new HashMap<>();
            dayParams.put("indexSort", dayOrder.getIndexSort());
            dayParams.put("tripDayId", dayOrder.getTripDayId());
            dayParams.put("tripId", tripId);
            int result = -1;
            try {
                result = tripMapper.updateTripDaysIndexSort(dayParams);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.TRIP_DAY_REORDER_FAILED);
            }
            if (result == 0) {      // 업데이트 결과에 따른 예외 처리
                throw new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND);
            }
        }
    }

    /* 여행 스케줄 [TRIP_SCHEDULE] ============================================================================================*/
    /**
     * 여행 스케줄 신규 추가
     * @param tripDayId 여행 일자 ID
     * @param request TripScheduleCreateRequest
     * @return TripScheduleCreateResponse
     */
    public TripScheduleCreateResponse createTripSchedule(String tripDayId, TripScheduleCreateRequest request) {
        Map<String, Object> scheduleParams = new HashMap<>();
        scheduleParams.put("tripDayId",      tripDayId);
        scheduleParams.put("indexSort",      request.getIndexSort());
        scheduleParams.put("startTime",      request.getStartTime());
        scheduleParams.put("endTime",        request.getEndTime());
        scheduleParams.put("bookmarkId",     request.getBookmarkId());
        scheduleParams.put("context",        request.getContext());
        scheduleParams.put("category",       request.getCategory());
        scheduleParams.put("price",          request.getPrice());
        scheduleParams.put("memo",           request.getMemo());
        scheduleParams.put("link",           request.getLink());
        scheduleParams.put("tripScheduleId", null);

        log.info(request);
        try {
            tripMapper.createTripSchedule(scheduleParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_CREATE_FAILED);
        }

        String tripScheduleId = (String) scheduleParams.get("tripScheduleId");
        return TripScheduleCreateResponse.builder()
                .tripScheduleId(tripScheduleId)
                .tripDayId(tripDayId)
                .indexSort(request.getIndexSort())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .context(request.getContext())
                .category(request.getCategory())
                .price(request.getPrice())
                .memo(request.getMemo())
                .link(request.getLink())
                .build();
    }

    /**
     * 여행 스케줄 단건 업데이트(수정/갱신)
     * @param tripScheduleId 여행 스케줄 ID
     * @param request TripScheduleUpdateRequest
     */
    public void updateTripSchedule(String tripScheduleId, TripScheduleUpdateRequest request) {
        Map<String, Object> scheduleParams = new HashMap<>();
        scheduleParams.put("tripScheduleId", tripScheduleId);
        scheduleParams.put("indexSort",      request.getIndexSort());
        scheduleParams.put("startTime",      request.getStartTime());
        scheduleParams.put("endTime",        request.getEndTime());
        scheduleParams.put("bookmarkId",     request.getBookmarkId());
        scheduleParams.put("context",        request.getContext());
        scheduleParams.put("category",       request.getCategory());
        scheduleParams.put("price",          request.getPrice());
        scheduleParams.put("memo",           request.getMemo());
        scheduleParams.put("link",           request.getLink());

        log.info(request);
        int result = -1;
        try {
            result = tripMapper.updateTripSchedule(scheduleParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_SAVE_FAILED);
        }

        if (result == 0) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
        }
    }

    /**
     * 여행 스케줄 순서 업데이트
     * case1. 같은 일자 내 스케줄 순서 변경
     * case2. 다른 일자로 스케줄 이동 및 순서 변경
     * @param request TripScheduleOrderUpdateRequest
     */
    @Transactional
    public void updateTripScheduleOrder(TripScheduleOrderUpdateRequest request) {
        // Day 리스트 순회 (같은 일자의 경우 1, 다른 일자의 경우 2의 크기를 갖음)
        for (TripScheduleOrderUpdateRequest.DayOrder dayOrder : request.getDayOrders()) {
            // Day 하위의 Schedule 리스트 업데이트 순회
            for (TripScheduleOrderUpdateRequest.ScheduleOrder schedule : dayOrder.getScheduleOrders()) {
                Map<String, Object> params = new HashMap<>();
                params.put("tripDayId",      dayOrder.getTripDayId());
                params.put("tripScheduleId", schedule.getTripScheduleId());
                params.put("indexSort",      schedule.getIndexSort());

                try {
                    tripMapper.updateTripScheduleIndexSort(params);
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.TRIP_SCHEDULE_REORDER_FAILED);
                }
            }
        }
    }

    /**
     * 여행 스케줄 단건 삭제
     * @param tripDayId 스케줄이 포함된 여행일자ID
     * @param tripScheduleId 여행스케줄 ID
     */
    @Transactional
    public void deleteTripSchedule(String tripDayId, String tripScheduleId) {
        // 1. SELECT TRIP SCHEDULE - getIndexSort
        int indexSort = -1;
        indexSort = tripMapper.readTripScheduleIndexSort(tripScheduleId);
        if (indexSort <= 0) throw new BusinessException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);

        // 2. DELETE TRIP SCHEDULE
        int result = -1;
        try {
            result = tripMapper.deleteTripSchedule(tripScheduleId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_DELETE_FAILED);
        }

        if (result == 0) {      // 삭제 결과가 없을 경우 예외 처리
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);
        }

        // 3. UPDATE INDEX SORT - tripDayId 기준 전체
        Map<String, Object> reorderParams = new HashMap<>();
        reorderParams.put("tripDayId", tripDayId);
        reorderParams.put("indexSort", indexSort);

        try {
            tripMapper.updateTripSchedulesIndexSortAfterDelete(reorderParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_REORDER_FAILED);
        }
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
}
