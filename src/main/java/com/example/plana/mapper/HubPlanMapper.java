package com.example.plana.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface HubPlanMapper {
    void createHubPlan(Map<String, Object> params);
    int checkHubPlanExists(String tripId);
    void updateHubPlanStatus(@Param("tripId") String tripId,
                             @Param("status") String status);

    String getTripIdByHubPlanId(String hubPlanId);
}
