package com.example.plana.mapper;

import com.example.plana.dto.trip.read.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TripMapper {
    // ── 소유자 확인 ─────────────────────────
    String readTripOwner(String tripId);

    // ── 존재 여부 파악 ─────────────────────────
    boolean existTripDay(String tripDayId);

    // ── 전체 조회 : Trip - TripDay - TripSchedule ────
    TripResponse readTrip(String tripId);
    List<TripDayResponse> readTripDaysByTripId(String tripId);
    List<TripScheduleResponse> readTripSchedulesByTripDayId(String tripDayId);

    // ── 상태(STATUS) 갱신 ───────────────────
    int updateTripStatus(Map<String, Object> params);
    void updateTripDaysStatus(Map<String, Object> params);
    void updateTripSchedulesStatus(Map<String, Object> params);

    // ── Trip ──────────────────────────────
    void createTrip(Map<String, Object> params);
    int updateTrip(Map<String, Object> params);
    int deleteTrip(String tripId);

    // ── TripDay ───────────────────────────
    void createTripDay(Map<String, Object> params);     // 여행 생성용 단건 INSERT
    int countTripDays(String tripId);                   // 여행 내 일자 수
    int updateTripDaysIndexSort(Map<String, Object> params);
    void deleteTripDaysByTripId(String tripId);
    // 단건 삭제 호출 ──────────────────────
    int readTripDayIndexSort(String tripDayId);             // 1. 삭제할 여행 일자 index sort 조회 SELECT
    int deleteTripDay(Map<String, Object> params);          // 2. 여행 일자 삭제 DELETE
    void updateTripDaysIndexSortAfterDelete(Map<String, Object> params);  // 3. 삭제 후 여행 일자 재정렬 UPDATE

    // ── TripSchedule ──────────────────────
    void createTripSchedule(Map<String, Object> params);   // 여행 생성용 단건 INSERT
    int updateTripSchedule(Map<String, Object> params);   // 단건 정보 수정 UPDATE
    void updateTripScheduleIndexSort(Map<String, Object> params);   // 순서 변경 UPDATE
    void deleteTripSchedulesByTripId(String tripId);        // 특정 여행ID 하위 전체 삭제 DELETE
    int deleteTripSchedulesByTripDayId(Map<String, Object> params); // 특정 여행ID 하위 전체 삭제 DELETE
    // 단건 삭제 호출 ──────────────────────
    int readTripScheduleIndexSort(String tripScheduleId);                       // 1. 삭제할 스케줄 INDEX_SORT 조회 SELECT
    int deleteTripSchedule(String tripScheduleId);                              // 2. 스케줄 단건 삭제 DELETE
    void updateTripSchedulesIndexSortAfterDelete(Map<String, Object> params);   // 3. tripDayId 기준 전체 INDEX_SORT 갱신 UPDATE
}
