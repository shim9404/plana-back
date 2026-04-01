package com.example.plana.mapper;

import com.example.plana.dto.trip.update.TripScheduleUpdateRequest;
import com.example.plana.dto.trip.update.TripUpdateRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TripMapper {
    void createTrip(Map<String, Object> params);
    void createTripDay(Map<String, Object> params);
    void createTripSchedule(Map<String, Object> params);
    void updateTrip(TripUpdateRequest request);
    void updateTripDays(TripUpdateRequest request);
    void updateTripSchedules(List<TripScheduleUpdateRequest> params);
}
