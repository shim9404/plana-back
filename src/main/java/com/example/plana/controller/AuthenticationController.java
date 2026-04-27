package com.example.plana.controller;

import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.auth.LogoutRequest;
import com.example.plana.dto.auth.TokenRefreshRequest;
import com.example.plana.dto.member.read.LoginRequest;
import com.example.plana.dto.member.read.LoginResponse;
import com.example.plana.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.plana.dto.common.ResponseBody;

import java.util.Map;

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
                ResponseBody.success(SuccessCode.LOGIN_SUCCESS, data));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ResponseBody> logout(@RequestBody LogoutRequest logoutRequest){
        log.info("logout request");
        authenticationService.logout(logoutRequest);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.LOGOUT_SUCCESS, null));
    }


    // 토큰 리프레시
    @PostMapping("/tokens/refresh")
    public ResponseEntity<ResponseBody> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest){

        LoginResponse data = authenticationService.tokenRefresh(tokenRefreshRequest.getRefreshToken());

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
    }

    /** DEV-161
     * 이메일 중복 체크
     *  -> existsEmail(): 이메일 중복 체크(true : 일치(중복) => ErrorCode 호출 / false: 비일치(중복 X) => 이메일 반환)
     * @param email        // 새 닉네임
     * @return ResponseBody.data : email
     */
    @GetMapping("/email/check")
    public ResponseEntity<ResponseBody> dupliEmail(@RequestParam String email) {
        authenticationService.existsEmail(email);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, Map.of("email", email)));
    }
}
