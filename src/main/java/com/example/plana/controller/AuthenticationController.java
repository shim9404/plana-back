package com.example.plana.controller;

import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.auth.*;
import com.example.plana.dto.common.EmptyData;
import com.example.plana.dto.member.read.LoginRequest;
import com.example.plana.dto.member.read.LoginResponse;
import com.example.plana.service.AuthenticationService;
import com.example.plana.service.MailAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Auth API", description = "인증 API 목록")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final MailAuthService mailAuthService;

    /**
     * 인증번호 발송
     */
    @PostMapping("/email/send")
    @Operation(summary = "인증 메일 전송", description = "회원가입을 위한 인증 메일을 전송한다.")
    @ApiResponse(responseCode = "200", description = "[S008] 인증번호를 발송했습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> sendMail(@Valid @RequestBody EmailSendRequest request) {
        log.info("sendMail request: {}", request);

        mailAuthService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.EMAIL_VERIFY_CODE_SENT));
    }

    /**
     * 인증번호 확인
     */
    @PostMapping("/email/verify")
    @Operation(summary = "인증 코드 검증", description = "이메일로 전달 받은 코드를 검증한다.")
    @ApiResponse(responseCode = "200", description = "[S009] 이메일 인증이 완료되었습니다.")
    public ResponseEntity<ResponseBody<EmailVerifyResponse>> verifyMail( @Valid @RequestBody EmailVerifyRequest request) {
        mailAuthService.verifyCode(request.getEmail(), request.getAuthCode());

        EmailVerifyResponse response = new EmailVerifyResponse(
                request.getEmail().trim().toLowerCase(),
                true
        );

        return ResponseEntity.ok(ResponseBody.success(SuccessCode.EMAIL_VERIFIED, response));
    }

    // 로그인
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "기존 회원 로그인한다.")
    @ApiResponse(responseCode = "200", description = "[S006] 로그인에 성공하였습니다.")
    public ResponseEntity<ResponseBody<LoginResponse>> login(@RequestBody LoginRequest loginRequest){
        LoginResponse data = authenticationService.signin(loginRequest);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.LOGIN_SUCCESS, data));
    }

    // 로그아웃
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그인한 회원의 로그아웃 처리를 진행한다.")
    @ApiResponse(responseCode = "200", description = "[S007] 안전하게 로그아웃되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> logout(@RequestBody LogoutRequest logoutRequest){

        authenticationService.logout(logoutRequest);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.LOGOUT_SUCCESS));
    }


    // 토큰 리프레시
    @PostMapping("/tokens/refresh")
    @Operation(summary = "로그인 인증 갱신", description = "로그인 상태 유지를 위해 토큰을 갱신한다.")
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<LoginResponse>> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest){

        LoginResponse data = authenticationService.tokenRefresh(tokenRefreshRequest.getRefreshToken());

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
    }

    /**
     * 이메일 중복 체크
     *  -> existsEmail(): 이메일 중복 체크(true : 일치(중복) => ErrorCode 호출 / false: 비일치(중복 X) => 이메일 반환)
     * @param email        // 새 닉네임
     * @return ResponseBody.data : email
     */
    @GetMapping("/email/check")
    @Operation(summary = "이메일 중복 체크", description = "전달한 email이 이미 가입되어있는지 판단 후 성공이나 실패를 반환한다.")
    @Parameters({ @Parameter(name = "email", description = "가입 여부를 확인할 이메일", required = true) })
    @ApiResponse(responseCode = "200", description = "[S001] 조회에 성공하였습니다.")
    public ResponseEntity<ResponseBody<DupliEmailResponse>> dupliEmail(@RequestParam String email) {
        DupliEmailResponse data = authenticationService.existsEmail(email);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }
}
