package com.example.plana.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface TripMapper {
    // ── Trip ──────────────────────────────
    void createTrip(Map<String, Object> params);
    void updateTrip(Map<String, Object> params);

    // ── TripDay ───────────────────────────
    void createTripDay(Map<String, Object> params);   // 여행 생성용 단건 INSERT
    void createTripDays(Map<String, Object> params);  // 복구용 전체 필드 INSERT (MERGE)
    void updateTripDays(Map<String, Object> params);  // 순서 변경 UPDATE (MERGE)
    void deleteTripDaysByTripId(String tripId);

    // ── TripSchedule ──────────────────────
    void createTripSchedule(Map<String, Object> params);   // 여행 생성용 단건 INSERT
    void createTripSchedules(Map<String, Object> params);  // 복구용 전체 필드 INSERT (MERGE)
    void updateTripSchedule(Map<String, Object> params);   // 단건 정보 수정 UPDATE
    void updateTripSchedules(Map<String, Object> params);  // 순서 변경 UPDATE (MERGE)
    void deleteTripSchedulesByTripId(String tripId);
}
