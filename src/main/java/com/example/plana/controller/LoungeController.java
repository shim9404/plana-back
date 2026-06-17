package com.example.plana.controller;

import com.example.plana.auth.CustomUserDetails;
import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.common.EmptyData;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.common.StatusUpdateRequest;
import com.example.plana.dto.trip.update.TripPublicUpdateRequest;
import com.example.plana.service.LoungeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ResponseBody<EmptyData>> updateHubPlanVisibility(@PathVariable String tripId, @RequestBody TripPublicUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {

        loungeService.updateHubPlanVisibility (tripId, request.getIsPublic(), principal.getMemberId());

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS));
    }

    @PatchMapping("/{hubPlanId}/status")
    @Operation(summary = "허브 게시물 상태 변경", description = "허브 게시물의 상태를 변경합니다. (ACTIVE / INACTIVE / DELETED)")
    @Parameters({ @Parameter(name = "hubPlanId", description = "상태를 변경할 허브 게시물 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> updateHubPlanStatus(@PathVariable String hubPlanId, @RequestBody StatusUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        loungeService.updateHupPlanStatus (hubPlanId, request.getStatus(), principal.getMemberId());

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS));
    }
}
