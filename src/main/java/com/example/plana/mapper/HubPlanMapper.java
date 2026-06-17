package com.example.plana.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface HubPlanMapper {
    void createHubPlan(Map<String, Object> params);             // 게시물 생성
    int checkHubPlanExists(String tripId);                      // 해당 여행의 게시물이 존재 여부 반환
    String updateHubPlanStatus(@Param("tripId") String tripId,
                             @Param("status") String status);   // 게시물 상태 업데이트

    String getTripIdByHubPlanId(String hubPlanId);              // 게시물에 연결된 여행 id 반환 
}
