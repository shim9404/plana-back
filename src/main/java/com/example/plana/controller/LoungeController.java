package com.example.plana.controller;

import com.example.plana.auth.CustomUserDetails;
import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.common.EmptyData;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.common.StatusUpdateRequest;
import com.example.plana.dto.lounge.HubPlanReadListResponse;
import com.example.plana.dto.lounge.HubPlanSearchRequest;
import com.example.plana.dto.lounge.LikePlanToggleResponse;
import com.example.plana.dto.lounge.MyTripForLoungeResponse;
import com.example.plana.dto.lounge.UpdateHubPlanPublicResponse;
import com.example.plana.dto.trip.read.HubPlanDetailResponse;
import com.example.plana.dto.trip.update.TripPublicUpdateRequest;
import com.example.plana.service.LoungeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lounge")
@RequiredArgsConstructor
@Tag(name = "Lounge API", description = "라운지 API 목록")
public class LoungeController {
    private final LoungeService loungeService;

    @PatchMapping("/{tripId}/public")
    @Operation(summary = "허브 공개 여부 설정", description = "여행을 허브에 공개하거나 비공개로 전환합니다. 최초 공개 시 게시물이 생성됩니다.")
    @Parameter(name = "tripId", description = "공개 여부를 변경할 여행 ID", required = true)
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<UpdateHubPlanPublicResponse>> updateHubPlanVisibility(@PathVariable String tripId, @RequestBody TripPublicUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        log.info("public:: "+ request.getIsPublic());
        log.info("getKeywords:: "+ request.getKeywords());
        UpdateHubPlanPublicResponse data = loungeService.updateHubPlanPublic(tripId, request.getIsPublic(), request.getKeywords(), principal.getMemberId());

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
    }

    @PatchMapping("/{hubPlanId}/status")
    @Operation(summary = "허브 게시물 상태 변경", description = "허브 게시물의 상태를 변경합니다. (INACTIVE / DELETED)")
    @Parameters({ @Parameter(name = "hubPlanId", description = "상태를 변경할 허브 게시물 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> updateHubPlanStatus(@PathVariable String hubPlanId, @RequestBody StatusUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        loungeService.updateHupPlanStatus (hubPlanId, request.getStatus(), principal.getMemberId());

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS));
    }


    @GetMapping("/hubs")
    @Operation(summary = "허브 목록 조회", description = "라운지에 공개된 허브 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "[S001] 조회에 성공하였습니다.")
    public ResponseEntity<ResponseBody<HubPlanReadListResponse>> readHubPlanList(@ParameterObject HubPlanSearchRequest request) {

        HubPlanReadListResponse data = loungeService.readHubPlanList(request);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }


    @GetMapping("hubs/{hubPlanId}")
    @Operation(summary = "허브 게시물 세부 조회", description = "허브 게시물의 세부 정보를 조회합니다.")
    @Parameter(name = "hubPlanId", description = "조회할 허브플랜 ID", required = true)
    @ApiResponse(responseCode = "200", description = "[S001] 조회가 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<HubPlanDetailResponse>> readHubPlanDetail(
            @PathVariable String hubPlanId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        String memberId = (principal != null) ? principal.getMemberId() : null;
        HubPlanDetailResponse data = loungeService.readHubPlanDetail(hubPlanId, memberId);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }

    @PatchMapping("/hubs/{hubPlanId}/like")
    @Operation(summary = "허브 게시물 좋아요 토글", description = "허브 게시물의 좋아요 상태를 토글합니다.")
    @Parameter(name = "hubPlanId", description = "좋아요 토글할 허브플랜 ID", required = true)
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<LikePlanToggleResponse>> toggleLikePlan(
            @PathVariable String hubPlanId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        LikePlanToggleResponse data = loungeService.toggleLikePlan(hubPlanId, principal.getMemberId());
        
        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
    }


    @GetMapping("/my-trips")
    @Operation(summary = "라운지 공개용 내 여행 목록 조회", description = "허브에 공개할 내 여행 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "[S001] 조회가 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<List<MyTripForLoungeResponse>>> readMyTripsForLounge(
            @AuthenticationPrincipal CustomUserDetails principal) {

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS,
                        loungeService.readMyTripsForLounge(principal.getMemberId())));
    }
}
