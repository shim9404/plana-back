package com.example.plana.service;

import com.example.plana.mapper.HubPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Log4j2
public class LoungeService {
    private final HubPlanMapper hubPlanMapper;
    private final TripService tripService;

    // trip을 hub_plan에 업로드 하는 api
    @Transactional
    public void updateHubPlanVisibility (String tripId, Boolean isPublic, String memberId) {
        // public으로 변환
        tripService.updateIsPublic(tripId, isPublic, memberId);

        // 없을 때
        if (hubPlanMapper.checkHubPlanExists(tripId) == 0){
            if (!isPublic) return;

            // 테이블에 업로드
            Map<String, Object> params = new HashMap<>();
            params.put("tripId", tripId);
            params.put("memberId", memberId);

            hubPlanMapper.createHubPlan(params);
        }
        else {
            // isPublic에 따라 status처리
            String status = isPublic ? "ACTIVE" : "INACTIVE";
            updateHupPlanStatusByTripId(tripId, status);
        }
    }

    // 여행 공개에 따른 상태 관리
    @Transactional
    public void updateHupPlanStatusByTripId (String tripId, String status){
        hubPlanMapper.updateHubPlanStatus(tripId, status);
    }

    @Transactional
    public void updateHupPlanStatus (String tripPlanId, String status, String memberId){
        String tripId = hubPlanMapper.getTripIdByHubPlanId(tripPlanId);

        hubPlanMapper.updateHubPlanStatus(tripId, status);

        if ("INACTIVE".equals(status) || "DELETED".equals(status)) {
            tripService.updateIsPublic(tripId, false, memberId);
        }
    }
}
