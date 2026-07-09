package com.example.plana.mapper;

import com.example.plana.dto.lounge.HubPlanStatResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TripStatMapper {
    int checkTripStatExists(String tripId);
    HubPlanStatResponse readTripStat(String tripId);
    void createTripStat(String tripId);
    void updateTripStat(String tripId);
    List<HubPlanStatResponse> readTripStatList(List<String> tripIds);
}
