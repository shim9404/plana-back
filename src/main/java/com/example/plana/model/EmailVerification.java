package com.example.plana.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 이메일 인증 정보 모델
 *
 * - JPA를 사용하지 않는 일반 Java 클래스
 * - MyBatis의 resultType / parameterType 으로 매핑 가능
 */
@Getter
@Setter
@NoArgsConstructor
public class EmailVerification {

    // PK
    private Long id;

    // 인증 대상 이메일
    private String email;

    // 인증번호
    private String authCode;

    // 인증 여부 (Y/N)
    private String verified;

    // 만료 시각
    private LocalDateTime expiredAt;

    // 생성 시각
    private LocalDateTime createdAt;

    // 인증 완료 시각
    private LocalDateTime verifiedAt;

    /**
     * 이메일 인증 생성용 생성자
     *
     * @param email 인증 대상 이메일
     * @param authCode 인증번호
     * @param expiredAt 만료 시각
     */
    public EmailVerification(String email, String authCode, LocalDateTime expiredAt) {
        this.email = email;
        this.authCode = authCode;
        this.verified = "N";
        this.expiredAt = expiredAt;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 전체 필드 생성자
     *
     * MyBatis 조회 결과를 수동 생성해야 할 때 사용할 수 있음
     */
    public EmailVerification(
            Long id,
            String email,
            String authCode,
            String verified,
            LocalDateTime expiredAt,
            LocalDateTime createdAt,
            LocalDateTime verifiedAt
    ) {
        this.id = id;
        this.email = email;
        this.authCode = authCode;
        this.verified = verified;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
        this.verifiedAt = verifiedAt;
    }

    /**
     * 인증 성공 처리
     */
    public void verify() {
        this.verified = "Y";
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * 인증번호 재발급 처리
     *
     * @param authCode 새 인증번호
     * @param expiredAt 새 만료 시각
     */
    public void refreshCode(String authCode, LocalDateTime expiredAt) {
        this.authCode = authCode;
        this.expiredAt = expiredAt;
        this.verified = "N";
        this.verifiedAt = null;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 만료 여부 확인
     *
     * @return 현재 시간이 만료 시각보다 이후이면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    /**
     * 인증 완료 여부 확인
     *
     * @return verified 값이 Y이면 true
     */
    public boolean isVerificationCompleted() {
        return "Y".equals(this.verified);
    }
}