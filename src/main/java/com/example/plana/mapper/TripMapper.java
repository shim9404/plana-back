package com.example.plana.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface TripMapper {
    void createTrip(Map<String, Object> params);
    void createTripDay(Map<String, Object> params);
    void createTripSchedule(Map<String, Object> params);
    void updateTrip(Map<String, Object> params);
    void updateTripDay(Map<String, Object> params);
    void updateTripSchedule(Map<String, Object> params);
}
