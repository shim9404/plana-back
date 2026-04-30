package com.example.plana.controller;

import com.example.plana.auth.CustomUserDetails;
import com.example.plana.auth.Role;
import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.member.update.MemberRoleUpdateRequest;
import com.example.plana.dto.member.update.MemberStatusUpdateRequest;
import com.example.plana.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/getAllMembers")
    public List<Map<String, Object>> getAllMembers(@RequestParam(required = false) Map<String, String> queryParams, @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
        log.info("getAllMembers query={}", queryParams);

        Map<String, Object> params = new HashMap<>();
        if (queryParams != null) {
            params.putAll(queryParams);
        }
        return adminService.getAllMembers(params);
    }

    @PatchMapping("/updateRole")
    public ResponseEntity<ResponseBody> updateMemberRole(@RequestBody MemberRoleUpdateRequest memberRoleUpdateRequest, @AuthenticationPrincipal CustomUserDetails principal){

        // 현재 로그인한 유저가 관리자인 경우만 허가
        if (principal.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        try {
            Role updatedRole = Role.valueOf(memberRoleUpdateRequest.getRole());
            adminService.updateMemberRole(memberRoleUpdateRequest.getMemberId(), updatedRole);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    @PatchMapping("/updateStatus")
    public ResponseEntity<ResponseBody> updateMemberStatus(@RequestBody MemberStatusUpdateRequest memberStatusUpdateRequest, @AuthenticationPrincipal CustomUserDetails principal){

        // 현재 로그인한 유저가 관리자인 경우만 허가
        if (principal.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        adminService.updateMemberStatus(memberStatusUpdateRequest.getMemberId(), memberStatusUpdateRequest.getStatus());

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }
}
