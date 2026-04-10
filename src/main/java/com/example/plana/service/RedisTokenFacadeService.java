package com.example.plana.service;


import com.example.plana.dto.redis.BlacklistRequest;
import com.example.plana.dto.redis.LoginFailRequest;
import com.example.plana.dto.redis.RefreshTokenRequest;
import com.example.plana.dto.redis.RefreshTokenResponse;
import com.example.plana.dto.redis.RefreshTokenValidateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * RedisTokenService를 실제 비즈니스 흐름에서 사용하기 위한 Facade Service
 *
 * 역할:
 * 1. Controller에서 받은 DTO를 RedisTokenService 호출 형태로 변환
 * 2. Redis 조회 결과를 응답 DTO로 변환
 * 3. Redis 관련 인증 기능을 중간 계층에서 묶어 제공
 *
 * 즉, Controller와 RedisTokenService 사이의 중간 서비스 역할을 한다.
 */

@Log4j2
@Service
@RequiredArgsConstructor
public class RedisTokenFacadeService {

    // 실제 Redis 저장/조회/삭제를 담당하는 서비스
    private final RedisTokenService redisTokenService;

    /**
     * 로그인 성공 시 등: Refresh Token 저장 ({@code refresh:{email}}).
     * DTO에서 필요한 값만 꺼내 실제 Redis 서비스 호출
     */
    public void saveRefreshToken(RefreshTokenRequest request) {
        log.info("saveRefreshToken : " + request);
        // DTO에서 필요한 값만 꺼내 실제 Redis 서비스 호출
        redisTokenService.saveRefreshToken(
                request.getEmail(),
                request.getRefreshToken(),
                request.getTtlMs()
        );
    }

    /**
     * 이메일 기준 Refresh Token 조회
     *
     * 동작:
     * - Redis에서 email 기준 refresh token 조회
     * - 조회 결과를 RefreshTokenResponse DTO로 만들어 반환
     *
     * @param email 사용자 이메일
     * @return email + tokenRefresh 형태의 응답 DTO
     */
    public RefreshTokenResponse getRefreshToken(String email) {
        // Redis에서 refresh token 문자열 조회
        String refreshToken = redisTokenService.getRefreshToken(email);

        // 컨트롤러에 바로 문자열을 넘기지 않고 응답 DTO로 조립립
        return RefreshTokenResponse.builder()
                .email(email)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 로그아웃 시 등: {@code refresh:{email}} 제거.
     */
    public void deleteRefreshToken(String email) {
        redisTokenService.deleteRefreshToken(email);
    }

    /**
     * 액세스 토큰 재발급 전: Redis에 둔 Refresh Token과 요청 토큰 일치 여부.
     */
    public boolean matchesStoredRefreshToken(RefreshTokenValidateRequest request) {
        log.info("matchesStoredRefreshToken : " + request);
        // 클라이언트 요청 DTO를 내부 서비스 파라미터 형식으로 변환
        return redisTokenService.matchesStoredRefreshToken(
                request.getEmail(),
                request.getRefreshToken()
        );
    }

    /**
     * Access Token 블랙리스트 등록
     *
     * 동작:
     * - 요청 DTO에서 accessToken, ttlMs 추출
     * - Redis 블랙리스트에 등록
     *
     * 사용 예:
     * - 로그아웃한 access token 즉시 무효화
     */
    public void addToBlacklist(BlacklistRequest request) {
        log.info("addToBlacklist : " + request);
        // accessToken과 ttlMs를 꺼내 블랙리스트 등록 위임임
        redisTokenService.addToBlacklist(
                request.getAccessToken(),
                request.getTtlMs()
        );
    }

    /**
     * Access Token 블랙리스트 여부 확인
     *
     * @param accessToken 검사할 access token
     * @return 블랙리스트 등록 여부
     */
    public boolean isBlacklisted(String accessToken) {
        return redisTokenService.isBlacklisted(accessToken);
    }

    /**
     * 로그인 실패 횟수 증가
     *
     * 동작:
     * - 요청 DTO에서 email 추출
     * - Redis에 저장된 로그인 실패 횟수를 1 증가
     * - 증가 후 현재 횟수 반환
     *
     * 사용 예:
     * - 로그인 실패 5회 이상 잠금 정책
     */
    public long increaseLoginFailCount(LoginFailRequest request) {
        log.info("increaseLoginFailCount : " + request);
        // 로그인 실패 DTO에서 email만 꺼내 실제 카운트 증가 수행행
        return redisTokenService.increaseLoginFailCount(request.getEmail());
    }

    /**
     * 로그인 실패 횟수 조회
     *
     * @param email 사용자 이메일
     * @return 현재 로그인 실패 횟수
     */
    public long getLoginFailCount(String email) {
        return redisTokenService.getLoginFailCount(email);
    }

    /**
     * 로그인 실패 횟수 초기화
     *
     * 동작:
     * - 로그인 성공 시 Redis에 저장된 실패 횟수 제거
     *
     * @param email 사용자 이메일
     */
    public void clearLoginFailCount(String email) {
        redisTokenService.clearLoginFailCount(email);
    }
}
