package com.example.plana.mapper;

import com.example.plana.dto.trip.read.TripScheduleOrderResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TripMapper {
    // ── Trip ──────────────────────────────
    void createTrip(Map<String, Object> params);
    int updateTrip(Map<String, Object> params);

    // ── TripDay ───────────────────────────
    void createTripDay(Map<String, Object> params);   // 여행 생성용 단건 INSERT
//    void createTripDays(Map<String, Object> params);  // 복구용 전체 필드 INSERT (MERGE)
//    void updateTripDays(Map<String, Object> params);  // 순서 변경 UPDATE (MERGE)
    int updateTripDaysIndexSort(Map<String, Object> params);
    int deleteTripDay(Map<String, Object> params);
    void deleteTripDaysByTripId(String tripId);

    // ── TripSchedule ──────────────────────
    void createTripSchedule(Map<String, Object> params);   // 여행 생성용 단건 INSERT
    int updateTripSchedule(Map<String, Object> params);   // 단건 정보 수정 UPDATE
    void updateTripSchedules(Map<String, Object> params);  // 순서 변경 UPDATE (MERGE)
    void deleteTripSchedulesByTripId(String tripId);        // 특정 여행ID 하위 전체 삭제 DELETE
    int deleteTripSchedulesByTripDayId(Map<String, Object> params); // 특정 여행ID 하위 전체 삭제 DELETE
    // 단건 삭제
    int deleteTripSchedule(String tripScheduleId);         // 단건 삭제 DELETE
    void updateTripSchedulesIndexSort(Map<String, Object> params);
    TripScheduleOrderResponse readTripScheduleIndexSort(String tripScheduleId);
    List<TripScheduleOrderResponse> readTripSchedulesIndexSort(String tripDayId);
}
