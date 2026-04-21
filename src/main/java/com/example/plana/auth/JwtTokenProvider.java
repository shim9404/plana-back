package com.example.plana.auth;


import com.example.plana.model.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Base64;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/*
JWT 토큰을 발급하는 스프링 컴포넌트(싱글톤)
이메일과 권한(ROLE_) 정보를 안전하게 암호화해서 토큰으로 반환
이 토큰으로 사용자의 인증/인가 처리
토큰은 만료시간이 있어, 보안성을 높이고, 세션리스서비스 구현에 적합
 */
@Component
public class JwtTokenProvider {
    //인코딩된 시크릿값
    private SecretKey SECRET_KEY;

    @Getter
    private final int refreshTokenTtlMillis;
    private final int accessTokenTtlMillis;

    public JwtTokenProvider(@Value("${jwt.secret}") String  secretKey, @Value("${jwt.access-token-expiration-ms}") int accessTokenExpiration, @Value("${jwt.refresh-token-expiration-ms}") int refreshTokenExpiration) {
        this.accessTokenTtlMillis = accessTokenExpiration;
        this.refreshTokenTtlMillis = refreshTokenExpiration;
        this.SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }//end of JwtTokenProvider

    /*
    Claims생성
    JWT의 payload 부분(=실제 데이터)에 들어갈 내용
    setSubject(memberId): 이 토큰의 주인은 memberId(주체정보)
    claims.put("role", role): 사용자 권한(로우 하이어라키)
    토큰 생성
    setIssuedAt(now): 토큰 발급 시간
    setExpiration(): 토큰 만료 시간(현재시간+유효시간)
    signWith(SECRET_KEY): Keys.hmacShaKeyFor로 만든 HMAC 비밀키로 서명(키 길이에 맞는 HMAC 알고리즘 선택)
    토큰 반환
    .compact(): JWT문자열 직렬화해서 반환
    이 토큰을 프론트엔드(리액트)에게 응답하면, 프론트는 이 토큰(access token)을 저장하고
    API요청할 때 마다 Authorization헤더에 넣어 인증
    */
    public String createAccessToken(String memberId, String role) {
        // JJWT 0.12+: Jwts.claims()...build() 결과는 불변이라 put 불가. 빌더에 subject/claim을 바로 설정.
        Date now = new Date();
        return Jwts.builder()
                .subject(memberId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenTtlMillis))
                .signWith(SECRET_KEY)
                .compact();
    }//end of createToken
    /*
    AccessToken 재발급에 사용(유효기간이 더 길다 - 여기서는 7일)
     */
    public String createRefreshToken(String memberId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(memberId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenTtlMillis))
                .signWith(SECRET_KEY)
                .compact();
    }//end of createToken

    //subject를 memberId 쓴다.
    public String extractSubject(String refreshToken) {
        //Claims::getSubject - 람다식
        //파라미터 즉 화면에서 넘어온 refreshToken에서 subject를 꺼냄.
        return extractClaim(refreshToken, Claims::getSubject);
    }//end of extractSubject

    // 만료된 토큰에서도 subject 추출
    public String extractSubjectIgnoreExpiry(String token) {
        try {
            return extractClaim(token, Claims::getSubject); // 정상 토큰이면 그대로
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject(); // ✅ 만료됐어도 claims에서 subject 추출
        }
    }


    //토큰에서 특정 값을 꺼내는 공용 메서드
    //<T> : 이 메서드는 T라는 타입을 사용한다.
    //아직 타입을 정하지 않은 반환타입을 T로 놓음.
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers){
        final Claims claims = extractAllClaims(token);//서명 검증 + payload
        return claimsResolvers.apply(claims);
    }//end of extractClaim

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Access JWT 만료 시각까지 남은 시간(ms). 이미 만료면 0. 파싱 실패 시 0.
     * Redis 블랙리스트 TTL을 남은 유효기간과 맞출 때 사용.
     */
    public long getAccessTokenRemainingTtlMillis(String accessToken) {
        try {
            Date exp = extractClaim(accessToken, Claims::getExpiration);
            return Math.max(0L, exp.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            return 0L;
        }
    }
    /**
     * Access/Refresh JWT 모두 subject에 {@code memberId}를 넣는다({@link #createAccessToken}, {@link #createRefreshToken}).
     * Refresh 재발급 시 회원과의 일치는 이메일이 아니라 회원 ID로 검증해야 한다.
     */
    public boolean isTokenValid(String token, Member member) {
        final String subject = extractSubject(token);
        return subject != null
                && member.getMemberId() != null
                && subject.equals(member.getMemberId())
                && !isTokenExpired(token);
    }
}