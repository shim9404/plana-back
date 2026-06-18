
package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.dto.lounge.HubPlanReadListResponse;
import com.example.plana.dto.lounge.HubPlanReadResponse;
import com.example.plana.dto.lounge.HubPlanSearchRequest;
import com.example.plana.dto.lounge.UpdateHubPlanPublicResponse;
import com.example.plana.mapper.HubPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Log4j2
public class LoungeService {
    private final HubPlanMapper hubPlanMapper;
    private final TripService tripService;

    /**
     * 허브 공개 여부 갱신 (신규 생성 포함)
     * @param tripId 갱신할 여행 ID
     * @param isPublic 공개 여부
     * @param memberId 사용자 ID
     * return String tripPlanId
     */
    @Transactional
    public UpdateHubPlanPublicResponse updateHubPlanPublic(String tripId, Boolean isPublic, String memberId) {
        String hubPlanId = "";
        // public으로 변환
        tripService.updateIsPublic(tripId, isPublic, memberId);

        // 없을 때
        if (hubPlanMapper.checkHubPlanExists(tripId) == 0){
            if (!isPublic) throw new BusinessException(ErrorCode.INVALID_HUB_PLAN_STATUS);

            // 테이블에 업로드
            Map<String, Object> params = new HashMap<>();
            params.put("tripId", tripId);
            params.put("memberId", memberId);

            try {
                hubPlanMapper.createHubPlan(params);
                hubPlanId = (String) params.get("hubPlanId");
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException(ErrorCode.HUB_PLAN_CREATE_FAILED);
            }
        }
        else {
            // isPublic에 따라 status처리
            String status = isPublic ? "ACTIVE" : "INACTIVE";
            hubPlanId = hubPlanMapper.updateHubPlanStatus(tripId, status);
        }

        return UpdateHubPlanPublicResponse.builder()
                .hubPlanId(hubPlanId)
                .build();
    }

    /**
     * 관리자용 허브 게시물 상태 업데이트
     * @param hubPlanId 허브 ID
     * @param status 갱신할 상태값
     * @param memberId 사용자 ID
     */
    @Transactional
    public void updateHupPlanStatus (String hubPlanId, String status, String memberId){

        String tripId = hubPlanMapper.getTripIdByHubPlanId(hubPlanId);

        if (tripId == null) { throw new BusinessException(ErrorCode.HUB_PLAN_NOT_FOUND); }

        // 강제 활성화 처리는 불가능
        if ("ACTIVE".equals(status)) { throw new BusinessException(ErrorCode.INVALID_HUB_PLAN_STATUS); }

        hubPlanMapper.updateHubPlanStatus(tripId, status);

        if ("INACTIVE".equals(status) || "DELETED".equals(status)) {
            tripService.updateIsPublic(tripId, false, memberId);
        }
    }


    /**
     * 허브플랜 목록 조회 (검색, 정렬, 페이징 포함)
     * @param request 검색 조건 (memberId, sortBy, page, size)
     * @return HubPlanReadListResponse 허브플랜 목록 및 페이징 정보
     */
    public HubPlanReadListResponse readHubPlanList(HubPlanSearchRequest request) {
        // 1. 목록 조회
        List<HubPlanReadResponse> plans = hubPlanMapper.readHubPlanList(request);

        // 2. 전체 개수 조회
        int totalCount = hubPlanMapper.countHubPlanList(request);

        // 3. 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalCount / request.getSize());

        return HubPlanReadListResponse.builder()
                .plans(plans)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .currentPage(request.getPage())
                .size(request.getSize())
                .build();
    }
}
