package com.example.plana.auth;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.service.RedisTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/*
JwtTokenFilter의 역할은 클라이언트가 요청을 할 때 토큰을 달고 다님
이 토큰이 정상적인 것인지 서버측에서 검증하는 과정이 필요한데 이것을 여기서 처리함
즉 토큰을 검증하는 코드를 작성해야 함.
 */
@Slf4j
@Component
public class JwtTokenFilter extends GenericFilter {


    private final RedisTokenService redisTokenService;

    private final HandlerExceptionResolver resolver;

    // 생성자를 직접 작성하여 @Qualifier 적용
    public JwtTokenFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
                            ,RedisTokenService redisTokenService) {
        this.resolver = resolver;
        this.redisTokenService = redisTokenService;
    }

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    /** SecurityConfig permitAll 과 동일 — JWT 없이 통과해야 하는 경로(로그인 시 옛 accessToken 헤더 무시) */
    private static final String[] JWT_EXEMPT_PATHS = {
            "/api/auth/**",
            "/api/members/nickname/**",
            "/api/members/email/**",
            "/api/regions/**",
            "/api/areas/**",
            "/api/redis/**",
            "/pds/**",
            "/api/members", // 회원가입
            "/error"
    };


    @Value("${jwt.secret}")
    private String secretKey;
    private SecretKey verificationKey;



    @PostConstruct
    void initVerificationKey() {
        verificationKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }

    private static String pathWithoutContext(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    private static boolean isJwtExemptPath(String path) {
        for (String pattern : JWT_EXEMPT_PATHS) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        log.info("request path: {}", pathWithoutContext(httpRequest)); // ✅ 여기 추가

        if (isJwtExemptPath(pathWithoutContext(httpRequest))) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("isExempt: {}", isJwtExemptPath(pathWithoutContext(httpRequest))); // ✅ 여기 추가
        String token = httpRequest.getHeader("Authorization");
        try{
            //token이 null이라는 건 토큰을 넣지 않았다는 것임
            if (token != null) {
                if (!token.startsWith("Bearer ") || token.length() < 8) {
                    throw new BusinessException(ErrorCode.MALFORMED_TOKEN); //("Bearer 형식이 아닙니다.");
                }
                //검증을 할 때는 Bearer를 떼어내고 검증함.
                String jwtToken = token.substring(7).trim();
                if (jwtToken.isEmpty()) {
                    filterChain.doFilter(request, response);
                    return;
                }

                if (redisTokenService.isBlacklisted(jwtToken)){
                    log.info("블랙리스트에 등록된 토큰입니다.");
                    throw new BusinessException(ErrorCode.INVALID_TOKEN);
                }
                //이 토큰을 가지고 검증하고 여기서 claims는 payload를 가리키는데
                //이것을 꺼내서 Authentication이라는 인증 객체를 만들 때 사용.
                Claims claims = Jwts.parser()
                        .verifyWith(verificationKey)
                        .build()
                        .parseSignedClaims(jwtToken)
                        .getPayload();//검증을 하고 Claims를 꺼내는 메서드임
                Role role = Role.valueOf((String) claims.get("role"));
                CustomUserDetails userDetails = new CustomUserDetails(claims.getSubject(), role);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, jwtToken, userDetails.getAuthorities());
                log.info("role:: "+role);
                log.info("authentication: {}", authentication.getName()); // ✅
                log.info("authorities: {}", authentication.getAuthorities()); // ✅
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            //아래 코드가 없으면 다음 필터로 연결이 안됨.
            //필터를 갔다가 다시 FilterChain으로 돌아가게 하는 코드임
            //토큰에 대한 확인이 되었으니 다시 원래 프로세스로 돌아간다.
            filterChain.doFilter(request, response);
        }catch (ExpiredJwtException e){
            e.printStackTrace();
            log.info("액세스 토큰 만료");
            resolver.resolveException(httpRequest, httpResponse, null, new BusinessException(ErrorCode.ACCESS_TOKEN_EXPIRED));
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            resolver.resolveException(httpRequest, httpResponse, null, e);
        }
    }
}