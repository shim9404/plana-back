package com.example.plana.controller;

import com.example.plana.auth.CustomUserDetails;
import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.common.EmptyData;
import com.example.plana.dto.common.ResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.plana.service.PointService;
import com.example.plana.dto.point.read.PointReadResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/points")
@Tag(name = "Point API", description = "포인트 API 목록")
public class PointController {
    private final PointService pointService;

    /**
     * getPoint(): 회원의 포인트 정보 호출 함수(포인트 페이지 진입)
     *  -> readPoint(): 포인트 정보 호출(포인트 내용, 금액, 잔액, .. 등)
     * @param memberId // 회원 고유 ID
     * @return ResponseBody.data : PointReadResponse
     */
    @GetMapping("/{memberId}")
    @Operation(summary = "포인트 정보 호출", description = "포인트 페이지 진입 시, 회원의 포인트 정보를 호출한다.")
    @Parameters({ @Parameter(name = "memberId", description = "회원 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S001] 조회에 성공하였습니다.")
    public ResponseEntity<ResponseBody<PointReadResponse>> getPoint(@PathVariable("memberId") String memberId, @AuthenticationPrincipal CustomUserDetails principal){
        PointReadResponse data = pointService.readPoint(principal.getMemberId(), memberId, principal.getRole());

        return ResponseEntity.ok(
                com.example.plana.dto.common.ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }

    /**
     * usePoint: 포인트 사용 함수
     *  -> createPointUse: 사용한 포인트 등록(여행 계획 생성 슬롯 수 추가)
     * @param memberId // 회원 고유 ID
     * @param event    // 포인트 이벤트
     * @return ResponseBody.data : null
     */
    @PostMapping("/{memberId}")
    @Operation(summary = "사용한 포인트 등록", description = "사용자가 포인트를 사용하고 사용 내역을 등록한다.")
    @Parameters({ @Parameter(name = "memberId", description = "회원 ID", required = true) })
    @ApiResponse(responseCode = "201", description = "[S002] 등록이 완료되었습니다.")

    public ResponseEntity<ResponseBody<EmptyData>> usePoint(@PathVariable("memberId") String memberId, @RequestParam("event") String event, @AuthenticationPrincipal CustomUserDetails principal) {
        pointService.createPointUse(principal.getMemberId(), memberId, principal.getRole(), event);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.INSERT_SUCCESS));

    }
}
