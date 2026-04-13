package com.example.plana.service;

import com.example.plana.auth.JwtTokenProvider;
import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.dto.auth.LogoutRequest;
import com.example.plana.dto.member.read.LoginRequest;
import com.example.plana.dto.member.read.LoginResponse;
import com.example.plana.model.Member;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final RedisTokenService redisTokenService;

    // 비밀번호 암호화
    private final PasswordEncoder passwordEncoder;

    // 이메일로 로그인
    public LoginResponse signin(LoginRequest loginRequest) {

        // 잠금된 계정인지 파악
        if (redisTokenService.isLoginLocked(loginRequest.getEmail())) {
            long sec = redisTokenService.getLoginLockRemainingSeconds(loginRequest.getEmail());
            throw new BusinessException(ErrorCode.LOCK_REMAIN_TIME, sec);
        }

        // 가입 여부 확인
        Member memberSavedData = memberService.readMemberByEmail(loginRequest.getEmail());
        if (memberSavedData == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), memberSavedData.getPassword())) {
            long fails = redisTokenService.increaseLoginFailCount(memberSavedData.getEmail());
            // 실패 제한 횟수
            int threshold = RedisTokenService.LOGIN_FAIL_LOCK_THRESHOLD;
            if (fails >= threshold) {
                // 잠금
                redisTokenService.setLoginLock(memberSavedData.getEmail());
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED, threshold);
            }
            int remaining = (int) (threshold - fails);
            // 잠금까지 남은 횟수 반환
            throw new BusinessException(ErrorCode.LOGIN_FAILED_COUNT, remaining);
        }
        log.info(memberSavedData);
        String memberId = memberSavedData.getMemberId();
        String email = memberSavedData.getEmail();
        String role = memberSavedData.getRole();
        String accessToken = jwtTokenProvider.createAccessToken(memberId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        log.info("signin success userId={} email={}", memberId, email);

        // Redis 노트: 로그인 성공 시 실패·잠금 초기화, refresh:{email} 에 Refresh Token 저장
        redisTokenService.clearLoginFailCount(email);
        redisTokenService.clearLoginLock(email);
        redisTokenService.saveRefreshToken(email, refreshToken, jwtTokenProvider.getRefreshTokenTtlMillis());

        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRole(role);
        loginResponse.setMemberId(memberId);

        return loginResponse;
    }//end of signin


    /*
  Refresh Token을 이용해서 Access Token / Refresh Token을 재발급하는 메서드
  처리 흐름
  1. 전달 받은 refreshToken에서 memberId 추출
  2. memberId 로 회원 조회
  3. tokenRefresh 자체 유효성 검사
  4. Redis에 저장된 refreshToken과 현재 요청 refreshToken이 같은지 비교
  5. 검증 통과시 새 accessToken / tokenRefresh 발급
  6. 새 refreshToken을 Redis에 저장
  7. 응답 DTO로 반환
   */
    public LoginResponse tokenRefresh(String refreshToken) {

        try {
            String memberId = jwtTokenProvider.extractSubject(refreshToken);
            Member memberSavedData = memberService.readMemberById(memberId);
            if (memberSavedData == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND); //"회원 정보를 찾을 수 없습니다."
            }
            //서명 위변조 여부 판단, 만료 여부 판단, 사용자 정보와 토큰 정보 일치 여부 등을 검사하여
            //유효하지 않으면 재발급 중담함.
            if (!jwtTokenProvider.isTokenValid(refreshToken, memberSavedData)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN); //"유효하지 않은 Refresh Token 입니다."
            }
            // Redis에 저장된 Refresh Token과 현재 요청으로 들어온 Refresh Token비교
            //로그인 시 Redis에 저장해둔 refresh token과 같아야 함.
            //토큰 도난, 재사용, 예전 refresh token 재사용 방지 목적
            //일치 하지 않으면 재발급 중단.
            if (!redisTokenService.matchesStoredRefreshToken(memberSavedData.getEmail(), refreshToken)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            // 새 Access Token 발급 (subject = memberId, 로그인 signin과 동일)
            String accessToken = jwtTokenProvider.createAccessToken(memberSavedData.getMemberId(), memberSavedData.getRole());
            // 새 Refresh Token (subject = member_id)
            String newRefreshToken = jwtTokenProvider.createRefreshToken(memberSavedData.getMemberId());
            log.info(refreshToken + ", " + newRefreshToken);
            log.info("이전값과 새로운 값 비교 : " + refreshToken.equals(newRefreshToken));
            // 새 Refresh Token을 Redis에 저장
            // key : refresh:{email}
            // value : newRefreshToken
            // ttl: refresh token 만료 시간과 동일하게 저장
            redisTokenService.saveRefreshToken(
                    memberSavedData.getEmail(),
                    newRefreshToken,
                    jwtTokenProvider.getRefreshTokenTtlMillis()
            );

            LoginResponse loginResponse = new LoginResponse();

            loginResponse.setRefreshToken(newRefreshToken);
            loginResponse.setAccessToken(accessToken);
            loginResponse.setMemberId(memberId);

            return loginResponse;
        } catch (ExpiredJwtException e) {
            // 리프레시 토큰 만료
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("토큰 재발급 실패", e);
            throw new BusinessException(ErrorCode.FAIL_REFRESH_TOKEN);
        }
    }//end of tokenRefresh


    /**
     * 로그아웃: Redis에서 refresh 삭제, 현재 Access Token을 blacklist:{token} 으로 등록.
     * Access Token은 서명·subject 검증 후 이메일이 일치할 때만 처리.
     * 1. 현재 Access Token을 블랙리스트에 등록
     * 2. Redis에 저장된 Refresh Token 삭제
     * 즉, 사용자가 로그아웃한 뒤에
     * 기존 Access Token으로 API 재호출하는 것 방지
     * 기존 Refresh Token으로 토큰 재발급하는 것 방지
     * 를 처리하는 코드입니다.
     *  ### 전체 흐름 ###
     *  1. 요청에서 email, accessToken 추출
     *  2. access token에서 email 추출
     *  3. 요청 email과 토큰 email이 같은지 검증
     *  4. access token 남은 유효시간 계산
     *  5. 남은 시간 있으면 블랙 리스트 등록
     *  6. Redis에 저장된 refresh token삭제
     *  7. 로그아웃 완료. 로그 출력
     */
    public void logout(LogoutRequest request) {
        log.info("access token: " + request.getAccessToken());
        // 1. 요청값 null/blank 검증
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (request.getAccessToken() == null || request.getAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String email = request.getEmail().trim();
        String accessToken = request.getAccessToken().trim();

        final String tokenMemberId;
        try {
            tokenMemberId = jwtTokenProvider.extractSubject(accessToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Member byEmail = memberService.readMemberByEmail(email);
        if (byEmail == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!byEmail.getMemberId().equals(tokenMemberId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED); // "토큰과 아이디가 일치하지 않습니다."
        }
        // Access Token 블랙 리스트 처리
        // JWT는 원래 발급 후 만료 전 까지 유효
        // 그래서 로그아웃해도 토큰 자체는 살아 있을 수 있음.
        // 이를 막기 위해 Redis에 blacklist:{accessToken} 형태로 저장
        // TTL은 토큰 남은 만료 시간 만큼만 저장. 즉 남은 30초면 Redis에도 30초만 저장되고 자동 삭제 됨.
        long ttlMs = jwtTokenProvider.getAccessTokenRemainingTtlMillis(accessToken);
        if (ttlMs > 0) {
            redisTokenService.addToBlacklist(accessToken, ttlMs);
        }
        // Refresh Token삭제
        // refresh:{email}키 삭제
        // 이후 토큰 재발급 불가
        // 실질적으로 세션 종료 처리
        redisTokenService.deleteRefreshToken(email);
        log.info("logout 완료 email={}", email);
    }

}
