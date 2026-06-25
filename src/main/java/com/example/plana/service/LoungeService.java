
package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.dto.lounge.*;
import com.example.plana.dto.region.read.RegionCodeResponse;
import com.example.plana.dto.trip.read.HubPlanDetailResponse;
import com.example.plana.dto.trip.read.TripResponse;
import com.example.plana.mapper.HubPlanMapper;
import com.example.plana.mapper.RegionMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Log4j2
public class LoungeService {
    private final HubPlanMapper hubPlanMapper;
    private final RegionMapper regionMapper;
    private final TripService tripService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
     * @param request 검색 조건
     * @return HubPlanReadListResponse 허브플랜 목록 및 페이징 정보
     */
    public HubPlanReadListResponse readHubPlanList(HubPlanSearchRequest request) {

        // 1. regionIds 분리 처리
        resolveRegionIds(request);

        // 2. 기본 목록 조회
        List<HubPlanReadResponse> plans = hubPlanMapper.readHubPlanList(request);

        // 3. 통계 조회 후 매핑
        if (!plans.isEmpty()) {
            mapStats(plans);
        }

        // 4. 페이징
        int totalCount = hubPlanMapper.countHubPlanList(request);
        int totalPages = (int) Math.ceil((double) totalCount / request.getSize());

        return HubPlanReadListResponse.builder()
                .plans(plans)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .currentPage(request.getPage())
                .size(request.getSize())
                .build();
    }

    /**
     * regionIds를 REGION 테이블 조회 후 zdoCodes / exactRegionIds 로 분리
     * SIGU_CODE == 0 이면 시/도 전체 선택 → zdoCodes에 ZDO_CODE 추가
     * 그 외에는 시군구 단위 정확 일치 → exactRegionIds에 추가
     * @param request 검색 조건 (regionIds 포함)
     */
    private void resolveRegionIds(HubPlanSearchRequest request) {
        if (request.getRegionIds() == null || request.getRegionIds().isEmpty()) return;

        List<String> exactRegionIds = new ArrayList<>();
        List<Integer> zdoCodes = new ArrayList<>();

        for (String regionId : request.getRegionIds()) {
            RegionCodeResponse regionCode = regionMapper.readRegionCodes(regionId);
            if (regionCode.getSiguCode() == 0) {
                zdoCodes.add(regionCode.getZdoCode());
            } else {
                exactRegionIds.add(regionId);
            }
        }

        request.setExactRegionIds(exactRegionIds);
        request.setZdoCodes(zdoCodes);
    }

    /**
     * tripId 목록으로 통계 일괄 조회 후 plans에 매핑
     * @param plans 기본 정보 목록
     */
    private void mapStats(List<HubPlanReadResponse> plans) {
        List<String> tripIds = plans.stream()
                .map(HubPlanReadResponse::getTripId)
                .collect(Collectors.toList());

        List<HubPlanStatResponse> statsList = hubPlanMapper.readHubPlanStatsList(tripIds);

        Map<String, HubPlanStatResponse> statsMap = statsList.stream()
                .collect(Collectors.toMap(HubPlanStatResponse::getTripId, s -> s));

        plans.forEach(plan -> {
            HubPlanStatResponse stat = statsMap.get(plan.getTripId());
            if (stat != null) {
                plan.setCategoryStatList(parseStats(stat));
                plan.setRegionStatList(parseRegionStats(stat));
            }
        });
    }

    /**
     * categoryStats JSON string → List<CategoryStatResponse> 파싱
     * @param stat 통계 응답
     * @return 카테고리 비율 목록
     */
    private List<CategoryStatResponse> parseStats(HubPlanStatResponse stat) {
        if (stat.getCategoryStats() == null) return Collections.emptyList();
        try {
            return objectMapper.readValue(
                    stat.getCategoryStats(),
                    new TypeReference<List<CategoryStatResponse>>() {}
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.HUB_PLAN_READ_FAILED);
        }
    }

    /**
     * regionStats JSON string → List<RegionStatResponse> 파싱
     * @param stat 통계 응답
     * @return 지역 비율 목록
     */
    private List<RegionStatResponse> parseRegionStats(HubPlanStatResponse stat) {
        if (stat.getRegionStats() == null) return Collections.emptyList();
        try {
            return objectMapper.readValue(
                    stat.getRegionStats(),
                    new TypeReference<List<RegionStatResponse>>() {}
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.HUB_PLAN_READ_FAILED);
        }
    }

    /**
     * 허브플랜 세부 조회
     * @param hubPlanId 허브플랜 ID
     * @param memberId  현재 로그인 유저 ID
     * @return HubPlanDetailResponse 여행 상세 + 허브 전용 정보
     */
    @Transactional(readOnly = true)
    public HubPlanDetailResponse readHubPlanDetail(String hubPlanId, String memberId) {

        // 1. hubPlanId로 tripId 조회
        String tripId = hubPlanMapper.getTripIdByHubPlanId(hubPlanId);
        log.info("hubPlanId:: "+hubPlanId);
        log.info("tripId:: "+tripId);
        if (tripId == null) throw new BusinessException(ErrorCode.HUB_PLAN_NOT_FOUND);

        // 2. 기존 여행 상세 재사용
        TripResponse tripDetail = tripService.readTrip(tripId, memberId);

        // 3. 허브 전용 추가 정보 조회 (좋아요/복사 수, 유저 좋아요/복사 여부, 작성자 정보)
        HubPlanInfoResponse hubInfo = hubPlanMapper.readHubPlanInfo(hubPlanId, memberId);

        // 4. 키워드 태그 조회
        List<String> keywordTags = hubPlanMapper.readHubPlanKeywords(hubPlanId);
        hubInfo.setKeywordTags(keywordTags);

        // 5. 조립 후 반환
        return HubPlanDetailResponse.builder()
                .tripDetail(tripDetail)
                .likeCount(hubInfo.getLikeCount())
                .copyCount(hubInfo.getCopyCount())
                .isLiked(hubInfo.getIsLiked())
                .isCopied(hubInfo.getIsCopied())
                .nickname(hubInfo.getNickname())
                .profileImage(hubInfo.getProfileImage())
                .keywordTags(keywordTags)
                .build();
    }
}
