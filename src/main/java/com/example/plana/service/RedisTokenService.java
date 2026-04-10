package com.example.plana.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 인증 상태용 Redis 유틸.
 * <p>
 * Refresh Token 키: {@code refresh:{email}} → tokenRefresh 문자열 (이메일은 trim+소문자로 통일)
 * <ul>
 *   <li><b>로그인 성공</b>: {@link #saveRefreshToken(String, String, long)} 으로 저장</li>
 *   <li><b>액세스 토큰 재발급</b>: {@link #matchesStoredRefreshToken(String, String)} 으로
 *       Redis 값과 클라이언트가 보낸 Refresh Token이 같은지 검증</li>
 *   <li><b>로그아웃</b>: {@link #deleteRefreshToken(String)} 으로 삭제</li>
 * </ul>
 * 로그인 실패/잠금: {@code login:fail:{email}}(1분 TTL 누적 카운트),
 * {@code login:lock:{email}}(3회 실패 시 2분 잠금).
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    // Redis 문자열 기반 작업용 템플릿
    private final StringRedisTemplate redisTemplate;

    // Refresh Token 저장용 key prefix
    private static final String REFRESH_PREFIX = "refresh:";

    // 로그아웃된 Access Token 블랙리스트 저장용 key prefix
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // 로그인 실패 횟수 저장용 key prefix (TTL 1분, 윈도우 내 누적 — 만료 시 자동 초기화)
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";

    /** 비밀번호 연속 실패 시 일시 잠금 — TTL 2분 */
    private static final String LOGIN_LOCK_PREFIX = "login:lock:";

    private static final Duration LOGIN_FAIL_WINDOW = Duration.ofMinutes(1);
    private static final Duration LOGIN_LOCK_TTL = Duration.ofMinutes(2);

    /** 이 횟수 이상 실패 시 {@link #LOGIN_LOCK_TTL} 동안 잠금 */
    public static final int LOGIN_FAIL_LOCK_THRESHOLD = 5;

    /**
     * Redis 키용 이메일 정규화: 공백 제거, NFKC(전각·호환 문자 정리), 소문자.
     */
    private static String normalizeEmailKey(String email) {
        if (email == null) {
            return "";
        }
        String s = Normalizer.normalize(email.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        return s;
    }

    /**
     * 로그인 성공 시 등: Refresh Token을 Redis에 저장한다.
     * 키: {@code refresh:{email}}
     *
     * @param email 사용자 이메일
     * @param refreshToken 저장할 refresh token
     * @param ttlMs 만료 시간(ms), JWT Refresh 만료와 맞추는 것을 권장
     */
    public void saveRefreshToken(String email, String refreshToken, long ttlMs) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            log.warn("saveRefreshToken: blank email");
            return;
        }
        String redisKey = REFRESH_PREFIX + ek;
        log.info("saveRefreshToken redisKey={} ttlMs={} (앱 로그의 이 키로 docker/redis-cli 에서 조회)", redisKey, ttlMs);
        redisTemplate.opsForValue().set(
                redisKey,
                refreshToken,
                Duration.ofMillis(ttlMs)
        );
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            log.error("Redis SET 직후 hasKey=false — 연결 대상이 다른 Redis이거나 DB 인덱스가 다를 수 있음 key={}", redisKey);
        }
    }

    /**
     * 이메일 기준 Refresh Token 조회
     *
     * @param email 사용자 이메일
     * @return 저장된 refresh token
     */
    public String getRefreshToken(String email) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            return null;
        }
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + ek);
    }

    /**
     * 로그아웃 시 등: Redis에 둔 Refresh Token을 제거한다.
     * <p>
     * JWT Access/Refresh 의 {@code subject}는 {@code member_id}이지만, 이 서비스에서 Refresh 를 붙이는
     * Redis 키는 {@link #saveRefreshToken} 과 같이 <b>항상 이메일</b>({@code refresh:}{@link #normalizeEmailKey 정규화된 email})이다.
     * 로그인·재발급 시에도 동일한 규칙으로 저장/비교하므로, 로그아웃 시에도 클라이언트가 알고 있는 <b>그 계정의 이메일</b>을 넘기는 것이 맞다.
     * {@code member_id}로 키를 쓰지 않는 이유를 바꾸려면 저장·매칭·삭제를 모두 같은 식별자 기준으로 바꿔야 한다.
     *
     * @param email 로그인에 쓰인 사용자 이메일(내부에서 trim·NFKC·소문자로 키에 맞춤)
     */
    public void deleteRefreshToken(String email) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            return;
        }
        redisTemplate.delete(REFRESH_PREFIX + ek);
    }

    /**
     * 토큰 재발급 전: Redis에 저장된 Refresh Token과 요청으로 온 토큰이 동일한지 검증한다.
     * 저장된 값이 없거나 다르면 {@code false}.
     */
    public boolean matchesStoredRefreshToken(String email, String refreshTokenFromClient) {
        log.info("matchesStoredRefreshToken : " + email + ", " + refreshTokenFromClient);
        // 요청  토큰이 없으면 비교할 필요 없이 false
        if (refreshTokenFromClient == null || refreshTokenFromClient.isEmpty()) {
            return false;
        }
        // Redis에 저장된 refresh token 조회
        String stored = getRefreshToken(email);
        // 저장된 값이 없으면 false
        if (stored == null || stored.isEmpty()) {
            return false;
        }
        // 문자열 비교를 안전하게 수행
        // 일반 equals대신 MessageDigest.isEqual() 사용
        byte[] a = stored.getBytes(StandardCharsets.UTF_8);
        byte[] b = refreshTokenFromClient.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    /**
     * 로그아웃된 Access Token을 블랙리스트에 등록
     * key: blacklist:{accessToken}
     * value: logout
     *
     * @param accessToken 로그아웃 처리된 access token
     * @param ttlMs access token 남은 만료 시간(ms)
     */
    public void addToBlacklist(String accessToken, long ttlMs) {
        log.info("addToBlacklist : " + accessToken + ", " + ttlMs);
        // 로그아웃된 access token을 블랙리스트 등록
        // TTL은 access token 남은 유효시간 만큼만 설정.
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "logout",
                Duration.ofMillis(ttlMs)
        );
    }

    /**
     * 현재 access token이 블랙리스트인지 확인
     *
     * @param accessToken 검사할 access token
     * @return 블랙리스트 여부
     */
    public boolean isBlacklisted(String accessToken) {
        // 해당 access token이 blacklist key로 존재하는지 확인
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }

    /**
     * 해당 이메일이 로그인 잠금 상태인지 (비밀번호 실패 3회로 인한 2분 잠금)
     */
    public boolean isLoginLocked(String email) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOGIN_LOCK_PREFIX + ek));
    }

    /**
     * 잠금 남은 시간(초). 잠금이 없으면 0.
     */
    public long getLoginLockRemainingSeconds(String email) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            return 0L;
        }
        Long ttl = redisTemplate.getExpire(LOGIN_LOCK_PREFIX + ek, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0L;
    }

    /**
     * 로그인 실패 후 2분 잠금 설정. 실패 카운트 키는 제거한다.
     */
    public void setLoginLock(String email) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            return;
        }
        String lockKey = LOGIN_LOCK_PREFIX + ek;
        redisTemplate.opsForValue().set(lockKey, "1", LOGIN_LOCK_TTL);
        redisTemplate.delete(LOGIN_FAIL_PREFIX + ek);
        log.info("setLoginLock emailNorm={} ttl={}", ek, LOGIN_LOCK_TTL);
    }

    /**
     * 로그인 성공 시 등: 잠금·실패 카운트 모두 해제
     */
    public void clearLoginLock(String email) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            return;
        }
        redisTemplate.delete(LOGIN_LOCK_PREFIX + ek);
    }

    /**
     * 비밀번호 불일치 시 실패 횟수 1 증가.
     * 카운트 키는 매번 {@link #LOGIN_FAIL_WINDOW}(1분) TTL을 갱신해,
     * 1분 동안의 누적 실패만 유효하고 키 만료 시 자동 초기화된다.
     *
     * @param email 사용자 이메일
     * @return 현재 실패 횟수
     */
    public long increaseLoginFailCount(String email) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            return 0L;
        }
        String key = LOGIN_FAIL_PREFIX + ek;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOGIN_FAIL_WINDOW);
        return count == null ? 0L : count;
    }

    /**
     * 현재 로그인 실패 횟수 조회
     *
     * @param email 사용자 이메일
     * @return 실패 횟수
     */
    public long getLoginFailCount(String email) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            return 0L;
        }
        String value = redisTemplate.opsForValue().get(LOGIN_FAIL_PREFIX + ek);
        return value == null ? 0L : Long.parseLong(value);
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     *
     * @param email 사용자 이메일
     */
    public void clearLoginFailCount(String email) {
        String ek = normalizeEmailKey(email);
        if (ek.isEmpty()) {
            return;
        }
        redisTemplate.delete(LOGIN_FAIL_PREFIX + ek);
    }
}

