package com.example.plana.service;

import com.example.plana.common.constants.TripConstatnts;
import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.common.utils.DateUtils;
import com.example.plana.component.TripAccessValidator;
import com.example.plana.dto.bookmark.create.BookmarkCopyRequest;
import com.example.plana.dto.bookmark.create.BookmarkCreateRequest;
import com.example.plana.dto.bookmark.read.BookmarkResponse;
import com.example.plana.dto.common.StatusUpdateRequest;
import com.example.plana.dto.trip.create.*;
import com.example.plana.dto.trip.read.TripDayResponse;
import com.example.plana.dto.trip.read.TripResponse;
import com.example.plana.dto.trip.read.TripScheduleResponse;
import com.example.plana.dto.trip.update.*;
import com.example.plana.mapper.BookmarkMapper;
import com.example.plana.mapper.TripMapper;
import com.example.plana.mapper.TripStatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class TripService {
    private final TripMapper tripMapper;
    private final BookmarkService bookmarkService;
    private final BookmarkMapper bookmarkMapper;
    private final CopyPlanService copyPlanService;
    private final TripStatService tripStatService;
    private final PointService pointService;

    private final TripAccessValidator tripAccessValidator;

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
        tripParams.put("regionId", request.getRegionId());
        tripParams.put("tripId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripId의 반환값을 담아야 함

        try {
            tripMapper.createTrip(tripParams);
            // +) 포인트 적립 [여행 계획 공유]
            pointService.createPointEarnTrip(request.getMemberId(), (String) tripParams.get("tripId"), (String)tripParams.get("name"));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_CREATE_FAILED);
        }
        String tripId = (String) tripParams.get("tripId");

        // 2. 날짜 범위 계산
        int diffDay = DateUtils.getDiffDay(tripParams.get("startDate").toString(),
                                            tripParams.get("endDate").toString());

        // 3. TRIP_DAY INSERT
        List<TripDayCreateResponse> dayList = new ArrayList<>();

        for (int i = 1; i <= diffDay; i++) {
            TripDayCreateResponse day = createTripDay(tripId, request.getMemberId());
            dayList.add(day);
        }

        // 4. 최종 응답 반환
        return TripCreateResponse.builder()
                .tripId(tripId)
                .name((String) tripParams.get("name"))
                .startDate((String) tripParams.get("startDate"))
                .endDate((String) tripParams.get("endDate"))
                .activeDayCount(diffDay)
                .regionId((String) tripParams.get("regionId"))
                .bookmarks(Collections.emptyList())
                .days(dayList)
                .build();
    }

    /**
     * 여행 단건 상세 조회 (소유자, 공유한 경우에만)
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     * @return TripResponse
     */
    @Transactional
    public TripResponse readTrip(String tripId, String memberId) {
        tripAccessValidator.validateAccess(tripId, memberId);
        return readTripDetail(tripId);
    }

    /**
     * 여행 단건 상세 조회
     * @param tripId 여행 ID
     * @return TripResponse
     */
    @Transactional
    private TripResponse readTripDetail(String tripId) {
        // 1. SELECT TRIP
        TripResponse trip = null;
        try {
            trip = tripMapper.readTrip(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_READ_FAILED);
        }

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

        // 4. SELECT BOOKMARKS
        trip.setBookmarks(bookmarkService.readBookmarksByTripId(tripId));

        // 5. activeDayCount (활성화 시킬 일자 수)
        int diffDay = DateUtils.getDiffDay(trip.getStartDate(),trip.getEndDate());
        trip.setActiveDayCount(diffDay);

        return trip;
    }


    /**
     * 여행 복제하여 내 여행으로 저장
     * // @Transactional : 여행 신규 복사(insert) - 여행 북마크 복사(insert) - 여행 일자 및 스케줄 복사(insert)을 하나의 트랜잭션으로
     * @param tripId String
     * @param memberId 사용자 ID
     * @param request TripCopyRequest
     * @return TripCopyResponse
     */
    @Transactional
    public TripCopyResponse copyTrip(String tripId, String memberId, TripCopyRequest request) {

        //tripId에 해당하는 여행이 복제 가능하도록 Public Open 상태인지 체크하는 로직
        tripAccessValidator.getIsPublic(tripId);

        // 1. TRIP INSERT - 복제할 여행 정보로 신규 여행 생성
        Map<String, Object> tripParams = new HashMap<>();
        tripParams.put("memberId", request.getMemberId());
        tripParams.put("name", request.getName());
        tripParams.put("startDate", request.getStartDate());
        tripParams.put("endDate", request.getEndDate());
        tripParams.put("regionId", request.getRegionId());
        tripParams.put("tripId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripId의 반환값을 담아야 함

        try {
            tripMapper.createTrip(tripParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_CREATE_FAILED);
        }
        String createdTripId = (String) tripParams.get("tripId");

        // 신규 등록된 북마크와 스케줄 link 작업용
        Map<String, String> bookmarkMatchs = new HashMap<>();

        // 2. BOOKMARK INSERT
        List<BookmarkResponse> bookmarks = new ArrayList<>();
        for (BookmarkCopyRequest bookmark: request.getBookmarks()) {
            Map<String, Object> bookmarkParam = new HashMap<>();
            bookmarkParam.put("tripId", createdTripId);
            bookmarkParam.put("memberId", memberId);
            bookmarkParam.put("bookmarkType", bookmark.getBookmarkType());
            bookmarkParam.put("areaId", bookmark.getAreaId());
            bookmarkParam.put("bookmarkId", null);  // OUT

            try {
                bookmarkMapper.createBookmark(bookmarkParam);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.TRIP_BOOKMARK_CREATE_FAILED);
            }
            String bookmarkId = (String) bookmarkParam.get("bookmarkId");
            bookmarks.add(BookmarkResponse.builder()
                            .bookmarkId(bookmarkId)
                            .bookmarkType(bookmark.getBookmarkType())
                            .areaId(bookmark.getAreaId())
                            .build());

            // 신규 등록된 북마크와 스케줄 link 작업용
            bookmarkMatchs.put(bookmark.getBookmarkId(), bookmarkId);
        }

        // 3. TRIP_DAY + TRIP_SCHEDULE 복사된 데이터로 삽입
        List<TripDayCreateResponse> dayList = new ArrayList<>();

        for (TripDayCopyRequest dayRequest : request.getDays()) {

            Map<String, Object> dayParams = new HashMap<>();
            dayParams.put("tripId",     createdTripId);
            dayParams.put("memberId",   memberId);
            dayParams.put("indexSort",  dayRequest.getIndexSort());
            dayParams.put("tripDayId",  null);

            try {
                tripMapper.createTripDay(dayParams);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.TRIP_DAY_CREATE_FAILED);
            }
            String tripDayId = (String) dayParams.get("tripDayId");

            List<TripScheduleCreateResponse> scheduleList = new ArrayList<>();

            for (TripScheduleCopyRequest scheduleRequest
                    : dayRequest.getSchedules()) {

                Map<String, Object> scheduleParams = new HashMap<>();
                scheduleParams.put("tripDayId",     tripDayId);
                scheduleParams.put("memberId",      memberId);
                scheduleParams.put("indexSort",     scheduleRequest.getIndexSort());
                scheduleParams.put("startTime",     scheduleRequest.getStartTime());
                scheduleParams.put("endTime",       scheduleRequest.getEndTime());
                scheduleParams.put("bookmarkId",    bookmarkMatchs.get(scheduleRequest.getBookmarkId()));   // 신규 등록된 북마크와 스케줄 link 매치
                scheduleParams.put("context",       scheduleRequest.getContext());
                scheduleParams.put("category",      scheduleRequest.getCategory());
                scheduleParams.put("price",         scheduleRequest.getPrice());
                scheduleParams.put("memo",          scheduleRequest.getMemo());
                scheduleParams.put("link",          scheduleRequest.getLink());
                scheduleParams.put("tripScheduleId", null);

                try {
                    tripMapper.copyTripSchedule(scheduleParams);
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.TRIP_SCHEDULE_CREATE_FAILED);
                }
                String tripScheduleId = (String) scheduleParams.get("tripScheduleId");

                scheduleList.add(TripScheduleCreateResponse.builder()
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

            dayList.add(TripDayCreateResponse.builder()
                    .tripDayId(tripDayId)
                    .indexSort(dayRequest.getIndexSort())
                    .schedules(scheduleList)
                    .build());
        }

        copyPlanService.recordCopyPlan(tripId, memberId);

        // 복제 성공 시 확인용 이름과 즉시 편집 요청을 위한 ID 반환
        return TripCopyResponse.builder()
                .tripId(createdTripId)
                .name((String) tripParams.get("name"))
                .build();
    }

    /**
     * 여행 정보(여행명, 참여 인원, 선택 지역) 갱신
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     * @param request TripInfoUpdateRequest
     */
    public void updateTripInfo(String tripId, String memberId, TripInfoUpdateRequest request) {
        tripAccessValidator.validateOwner(tripId, memberId);

        Map<String, Object> tripParams = new HashMap<>();
        tripParams.put("tripId"     , tripId);
        tripParams.put("name"       , request.getName());
        tripParams.put("entryCount" , request.getEntryCount());
        tripParams.put("regionId" , request.getRegionId());
        log.info(request);

        try {
            tripMapper.updateTrip(tripParams);
            // +) 포인트 적립 수정(여행명 변경) [여행 계획 생성]
            pointService.updatePointEarnTrip(memberId, tripId, request.getName());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_UPDATE_FAILED);
        }
    }

    /**
     * 여행 일자(startDate, endDate) 갱신 및 부족한 일자 생성 후 반환
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     * @param request TripDateUpdateRequest
     * @return TripDateUpdateResponse
     */
    @Transactional
    public TripDateUpdateResponse updateTripDate(String tripId, String memberId, TripDateUpdateRequest request) {
        tripAccessValidator.validateOwner(tripId, memberId);

        // 1. 여행 정보 저장
        Map<String, Object> tripParams = new HashMap<>();
        tripParams.put("tripId"     , tripId);
        tripParams.put("startDate"  , request.getStartDate());
        tripParams.put("endDate"    , request.getEndDate());
        try {
            tripMapper.updateTrip(tripParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_UPDATE_FAILED);
        }

        // 2. 현재 여행에 있는 일자 수 조회
        int currentDayCount;
        try {
            currentDayCount = tripMapper.countTripDays(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_READ_FAILED);
        }

        // 3. 변경된 날짜 범위 계산
        int targetDayCount = DateUtils.getDiffDay(request.getStartDate(), request.getEndDate());

        // 4. 비교해서 부족한 수만큼 일자 생성 진행
        List<TripDayCreateResponse> addDays = new ArrayList<>();
        if (currentDayCount < targetDayCount) {
            for (int i = currentDayCount; i < targetDayCount; i++) {
                TripDayCreateResponse day = createTripDay(tripId, memberId);
                addDays.add(day);
            }
        }

        // 5. 추가된 일자 목록을 포함하여 반환
        return TripDateUpdateResponse.builder()
                .tripId(tripId)
                .addDays(addDays)
                .startDate((String) tripParams.get("startDate"))
                .endDate((String) tripParams.get("endDate"))
                .activeDayCount(targetDayCount)
                .build();
    }

    /**
     * 여행 상태(Status) 갱신
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     * @param request TripStatusUpdateResponse : STATUS - ACTIVE(활성) / INACTIVE(비활성) / DELETED(삭제)
     */
    @Transactional
    public void updateTripStatus(String tripId, String memberId, StatusUpdateRequest request) {

        tripAccessValidator.validateOwner(tripId, memberId);

        Map<String, Object> statusParams = new HashMap<>();
        statusParams.put("tripId"       , tripId);
        statusParams.put("status"       , request.getStatus());

        // 여행 상태 갱신
        try {
            tripMapper.updateTripStatus(statusParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_UPDATE_FAILED);
        }

        // 여행 일자 상태 갱신
        try {
            tripMapper.updateTripDaysStatus(statusParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_UPDATE_FAILED);
        }
        // 여행 스케줄 상태 갱신
        try {
            tripMapper.updateTripSchedulesStatus(statusParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_UPDATE_FAILED);
        }

        // 북마크 상태 갱신
        bookmarkService.updateBookmarksStatus(tripId, request);
    }

    /**
     * 여행 단건 삭제 - 하위 일자 및 스케줄 전체 삭제
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     */
    @Transactional
    public void deleteTrip(String tripId, String memberId) {

        tripAccessValidator.validateOwner(tripId, memberId);

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
        // 북마크 삭제
        bookmarkService.deleteBookmarksByTripId(tripId);

        // 2. 여행 삭제
        int result = -1;
        try {
            result = tripMapper.deleteTrip(tripId);
            // +) 포인트 만료 [24시간 내 여행 계획 삭제]
            pointService.createPointExpireTrip(memberId, tripId);
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
     * @param memberId 사용자 ID
     * @return TripDayCreateResponse
     */
    @Transactional
    public TripDayCreateResponse createTripDay(String tripId, String memberId) {

        tripAccessValidator.validateOwner(tripId, memberId);
        
        // 1. TRIP_DAY INSERT (단건)
        Map<String, Object> dayParams = new HashMap<>();
        dayParams.put("tripId", tripId);
        dayParams.put("memberId", memberId);
        dayParams.put("indexSort", null);   // OUT
        dayParams.put("tripDayId", null);   // OUT : Insert 요청 후, 트리거로 생성된 tripDayId의 반환값을 담아야 함

        try {
            tripMapper.createTripDay(dayParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_CREATE_FAILED);
        }
        String tripDayId = (String) dayParams.get("tripDayId");
        int dayIndexSort = (int) dayParams.get("indexSort");

        // 2. TRIP_SCHEDULE INSERT (단건)
        Map<String, Object> scheduleParams = new HashMap<>();
        scheduleParams.put("tripDayId", tripDayId);
        scheduleParams.put("memberId", memberId);
        scheduleParams.put("indexSort", dayIndexSort);
        scheduleParams.put("tripScheduleId", null);  // OUT : Insert 요청 후, 트리거로 생성된 tripScheduleId의 반환값을 담아야 함

        try {
            tripMapper.createTripSchedule(scheduleParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_CREATE_FAILED);
        }
        String tripScheduleId = (String) scheduleParams.get("tripScheduleId");
        int scheduleIndexSort = (int) scheduleParams.get("indexSort");

        // 3. 일자와 스케줄의 Response 데이터에 담아 조립
        TripScheduleCreateResponse schedule = TripScheduleCreateResponse.builder()
                .tripScheduleId(tripScheduleId)
                .indexSort(scheduleIndexSort)
                .build();

        // 4. 최종 응답 반환
        return TripDayCreateResponse.builder()
                .tripDayId(tripDayId)
                .indexSort(dayIndexSort)
                .schedules(List.of(schedule))
                .build();
    }

    /**
     * 여행 일자 삭제(여행 일자 하위 스케줄 포함)
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @param memberId 사용자 ID
     */
    @Transactional
    public void deleteTripDay(String tripId, String tripDayId, String memberId) {
        tripAccessValidator.validateOwner(tripId, memberId);
        
        // 여행 일자가 존재하지 않음
        if (!tripMapper.existTripDay(tripDayId)) {  throw new BusinessException(ErrorCode.TRIP_DAY_NOT_FOUND);  }

        Map<String, Object> deleteParams = new HashMap<>();
        deleteParams.put("tripId", tripId);
        deleteParams.put("tripDayId", tripDayId);
        // 1. SELECT TRIP DAY - getIndexSort
        int indexSort = -1;
        indexSort = tripMapper.readTripDayIndexSort(tripDayId);

        // 2. 여행 스케줄 전체 삭제
        int result = -1;
        try {
            result = tripMapper.deleteTripSchedulesByTripDayId(deleteParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_DELETE_FAILED);
        }

        // 3. 여행 일자 삭제
        try {
            tripMapper.deleteTripDay(deleteParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_DAY_DELETE_FAILED);
        }

        // 4. 삭제된 일자 이후의 index sort 재정렬
        Map<String, Object> reorderParams = new HashMap<>();
        reorderParams.put("tripId", tripId);
        reorderParams.put("memberId", memberId);
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
    public void updateTripDaysIndexSort(String tripId, String memberId, TripDayOrderUpdateRequest request) {

        tripAccessValidator.validateOwner(tripId, memberId);

        for (TripDayOrderUpdateRequest.DayOrder dayOrder : request.getDayOrders()) {
            Map<String, Object> dayParams = new HashMap<>();
            dayParams.put("indexSort", dayOrder.getIndexSort());
            dayParams.put("tripDayId", dayOrder.getTripDayId());
            dayParams.put("tripId", tripId);
            dayParams.put("memberId", memberId);

            try {
                tripMapper.updateTripDaysIndexSort(dayParams);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.TRIP_DAY_REORDER_FAILED);
            }
        }
    }

    /* 여행 스케줄 [TRIP_SCHEDULE] ============================================================================================*/
    /**
     * 여행 스케줄 신규 추가
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @param memberId 사용자 ID
     * @return TripScheduleCreateResponse
     */
    public TripScheduleCreateResponse createTripSchedule(String tripId, String tripDayId, String memberId/*, TripScheduleCreateRequest request*/) {
        tripAccessValidator.validateOwner(tripId, memberId);

        Map<String, Object> scheduleParams = new HashMap<>();
        scheduleParams.put("tripDayId",      tripDayId);
        scheduleParams.put("memberId",       memberId);
        scheduleParams.put("indexSort",      null);     // OUT
        scheduleParams.put("tripScheduleId", null);     // OUT

        log.info(scheduleParams);
        try {
            tripMapper.createTripSchedule(scheduleParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_CREATE_FAILED);
        }

        // 통계 갱신
        tripStatService.refreshTripStat(tripId);

        String tripScheduleId = (String) scheduleParams.get("tripScheduleId");
        int indexSort = (int) scheduleParams.get("indexSort");
        return TripScheduleCreateResponse.builder()
                .tripScheduleId(tripScheduleId)
                .tripDayId(tripDayId)
                .indexSort(indexSort)
                .build();
    }

    /**
     * 여행 스케줄 단건 업데이트(수정/갱신)
     * @param tripScheduleId 여행 스케줄 ID
     * @param memberId 사용자 ID
     * @param request TripScheduleUpdateRequest
     */
    public void updateTripSchedule(String tripId, String tripScheduleId, String memberId, TripScheduleUpdateRequest request) {
        tripAccessValidator.validateOwner(tripId, memberId);

        Map<String, Object> scheduleParams = new HashMap<>();
        scheduleParams.put("tripScheduleId", tripScheduleId);
        scheduleParams.put("memberId",       memberId);
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
        try {
            tripMapper.updateTripSchedule(scheduleParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_UPDATE_FAILED);
        }

        // 통계 갱신
        tripStatService.refreshTripStat(tripId);
    }

    /**
     * 여행 스케줄 순서 업데이트
     * case1. 같은 일자 내 스케줄 순서 변경
     * case2. 다른 일자로 스케줄 이동 및 순서 변경
     * @param memberId 사용자 ID
     * @param request TripScheduleOrderUpdateRequest
     */
    @Transactional
    public void updateTripScheduleOrder(String tripId, String memberId, TripScheduleOrderUpdateRequest request) {
        tripAccessValidator.validateOwner(tripId, memberId);

        // Day 리스트 순회 (같은 일자의 경우 1, 다른 일자의 경우 2의 크기를 갖음)
        for (TripScheduleOrderUpdateRequest.DayOrder dayOrder : request.getDayOrders()) {
            // Day 하위의 Schedule 리스트 업데이트 순회
            for (TripScheduleOrderUpdateRequest.ScheduleOrder schedule : dayOrder.getScheduleOrders()) {
                Map<String, Object> params = new HashMap<>();
                params.put("tripDayId",      dayOrder.getTripDayId());
                params.put("memberId",      memberId);
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
     * @param memberId 사용자 ID
     * @param tripScheduleId 여행스케줄 ID
     */
    @Transactional
    public void deleteTripSchedule(String tripId, String tripDayId, String tripScheduleId, String memberId) {
        tripAccessValidator.validateOwner(tripId, memberId);

        // 1. SELECT TRIP SCHEDULE - getIndexSort
        int indexSort = -1;
        indexSort = tripMapper.readTripScheduleIndexSort(tripScheduleId);
        if (indexSort <= 0) throw new BusinessException(ErrorCode.TRIP_SCHEDULE_NOT_FOUND);

        // 2. DELETE TRIP SCHEDULE
        try {
            tripMapper.deleteTripSchedule(tripScheduleId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_DELETE_FAILED);
        }

        // 3. UPDATE INDEX SORT - tripDayId 기준 전체
        Map<String, Object> reorderParams = new HashMap<>();
        reorderParams.put("tripDayId", tripDayId);
        reorderParams.put("memberId", memberId);
        reorderParams.put("indexSort", indexSort);

        try {
            tripMapper.updateTripSchedulesIndexSortAfterDelete(reorderParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SCHEDULE_REORDER_FAILED);
        }

        // 통계 갱신
        tripStatService.refreshTripStat(tripId);
    }

    /**
     * 여행 공유를 위한 토큰 갱신(신규 발급 포함)
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     * @return TripShareTokenResponse
     */
    @Transactional
    public TripShareTokenResponse createShareToken(String tripId, String memberId) {

        // 소유자 검증
        tripAccessValidator.validateOwner(tripId, memberId);

        // 토큰 생성
        String shareToken = UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> params = new HashMap<>();
        params.put("tripId",     tripId);
        params.put("memberId",   memberId);
        params.put("shareToken", shareToken);

        try {
            tripMapper.updateShareToken(params);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SHARE_TOKEN_CREATE_FAILED);
        }

        return TripShareTokenResponse.builder()
                .tripId(tripId)
                .shareToken(shareToken)
                .build();
    }

    /**
     * 여행 공유 중단(공유 토큰 삭제)
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     */
    @Transactional
    public void deleteShareToken(String tripId, String memberId) {

        // 소유자 검증
        tripAccessValidator.validateOwner(tripId, memberId);

        // 토큰 무효화
        Map<String, Object> params = new HashMap<>();
        params.put("tripId",   tripId);
        params.put("memberId", memberId);

        try {
            tripMapper.deleteShareToken(params);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_SHARE_TOKEN_DELETE_FAILED);
        }
    }

    /**
     * 공유된 여행 단건 상세 조회
     * @param shareToken 공유 토큰
     * @return TripResponse
     */
    @Transactional
    public TripResponse readSharedTrip(String shareToken) {
        // shareToken으로 trip 찾기

        // 1. SELECT TRIP
        TripResponse trip = null;
        try {
            trip = tripMapper.readSharedTrip(shareToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_READ_FAILED);
        }
        if (trip == null) {
            throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);
        }

        String tripId = trip.getTripId();

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

        // 4. SELECT BOOKMARKS
        trip.setBookmarks(bookmarkService.readBookmarksByTripId(tripId));

        // 5. activeDayCount (활성화 시킬 일자 수)
        int diffDay = DateUtils.getDiffDay(trip.getStartDate(),trip.getEndDate());
        trip.setActiveDayCount(diffDay);

        return trip;
    }


    /**
     * 북마크 생성
     * - 유효성 검증을 위해 한 번 거치는 작업
     * @param tripId 여행일자ID
     * @param memberId 사용자 ID
     * @param request BookmarkCreateRequest ID
     */
    public BookmarkResponse createBookmark(String tripId, String memberId, BookmarkCreateRequest request){
        tripAccessValidator.validateOwner(tripId, memberId);
        return bookmarkService.createBookmark(tripId, memberId, request);
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
     *  허브 공개 여부 갱신
     * - 허브에 공개 시 true 비공개 전환 시 false
     * - 현재는 false여도 공개한적 있는지 여부는 hub_plan에서 구분할 수 있음
     * @param tripId 여행ID
     * @param isPublic 허브에 공개여부
     * @param memberId 사용자 ID
     */
    public void updateIsPublic(String tripId, Boolean isPublic, String memberId) {
        tripAccessValidator.validateOwner(tripId, memberId);

        String value = isPublic ? "Y" : "N";

        Map<String, Object> params = new HashMap<>();
        params.put("tripId",      tripId);
        params.put("isPublic",    value);
        
        tripMapper.updateIsPublic(params);
    }

    /**
     * 허브를 통한 여행 상세 조회 (소유자 검증 없음)
     * @param tripId 여행 ID
     * @return TripResponse
     */
    public TripResponse readTripDetailForHub(String tripId) {

        if (!tripAccessValidator.getIsPublic(tripId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        return readTripDetail(tripId);
    }
}
