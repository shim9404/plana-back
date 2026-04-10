package com.example.plana.controller;

import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.auth.TokenRefreshRequest;
import com.example.plana.dto.member.read.LoginRequest;
import com.example.plana.dto.member.read.LoginResponse;
import com.example.plana.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.plana.dto.common.ResponseBody;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseBody> login(@RequestBody LoginRequest loginRequest){
        LoginResponse data = authenticationService.signin(loginRequest);

        return ResponseEntity.ok(
                com.example.plana.dto.common.ResponseBody.success(SuccessCode.LOGIN_SUCCESS, data));
    }

    // 토큰 리프레시
    @PostMapping("/tokens/refresh")
    public ResponseEntity<ResponseBody> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest){

        LoginResponse data = authenticationService.tokenRefresh(tokenRefreshRequest.getRefreshToken());

        return ResponseEntity.ok(
                com.example.plana.dto.common.ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
    }
}
