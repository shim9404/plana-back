package com.example.plana.mapper;

import com.example.plana.dto.lounge.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface HubPlanMapper {
    void createHubPlan(Map<String, Object> params);             // 게시물 생성
    int checkHubPlanExists(String tripId);                      // 해당 여행의 게시물이 존재 여부 반환
    String updateHubPlanStatus(@Param("tripId") String tripId,
                             @Param("status") String status);   // 게시물 상태 업데이트

    String getTripIdByHubPlanId(String hubPlanId);              // 게시물에 연결된 여행 id 반환
    List<HubPlanReadResponse> readHubPlanList(HubPlanSearchRequest request);

    int countHubPlanList(HubPlanSearchRequest request);

    HubPlanInfoResponse readHubPlanInfo(@Param("hubPlanId") String hubPlanId,
                                        @Param("memberId") String memberId);

    List<String> readHubPlanKeywords(String hubPlanId);


    int checkLikePlanExists(@Param("hubPlanId") String hubPlanId, @Param("memberId") String memberId);

    void createLikePlan(@Param("hubPlanId") String hubPlanId, @Param("memberId") String memberId);

    void updateLikePlanStatus(@Param("hubPlanId") String hubPlanId,
                              @Param("memberId") String memberId,
                              @Param("status") String status);

    void updateHubPlanLikeCount(@Param("hubPlanId") String hubPlanId, @Param("delta") int delta);

    LikePlanToggleResponse readLikePlanStatus(@Param("hubPlanId") String hubPlanId,
                                              @Param("memberId") String memberId);

    String getHubPlanIdByTripId(String tripId);

    int checkCopyPlanExists(@Param("hubPlanId") String hubPlanId, @Param("memberId") String memberId);

    void createCopyPlan(@Param("hubPlanId") String hubPlanId, @Param("memberId") String memberId);

    void updateCopyPlanStatus(@Param("hubPlanId") String hubPlanId,
                              @Param("memberId") String memberId,
                              @Param("status") String status);

    void updateHubPlanCopyCount(@Param("hubPlanId") String hubPlanId, @Param("delta") int delta);
}
