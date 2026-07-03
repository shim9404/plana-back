package com.example.plana.service;

import com.example.plana.component.TripAccessValidator;
import com.example.plana.mapper.HubPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CopyPlanService {

    private final HubPlanMapper hubPlanMapper;
    private final TripAccessValidator tripAccessValidator;

    @Transactional
    public void recordCopyPlan(String tripId, String memberId) {

        String hubPlanId = hubPlanMapper.getHubPlanIdByTripId(tripId);

        if (hubPlanId == null) return;
        if (tripAccessValidator.getIsOwner(tripId, memberId)) return;

        int exists = hubPlanMapper.checkCopyPlanExists(hubPlanId, memberId);

        // 처음 복제하는 경우에만 기록
        if (exists == 0) {
            hubPlanMapper.createCopyPlan(hubPlanId, memberId);
            hubPlanMapper.updateHubPlanCopyCount(hubPlanId, 1);
        }
    }
}
