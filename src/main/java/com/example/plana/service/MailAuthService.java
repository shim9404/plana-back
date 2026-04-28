package com.example.plana.service;


import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.mapper.AuthenticationMapper;
import com.example.plana.mapper.EmailVerificationMapper;
import com.example.plana.mapper.MemberMapper;
import com.example.plana.model.EmailVerification;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class MailAuthService {
    private final EmailVerificationMapper emailVerificationMapper;
    private final MemberMapper memberMapper;
    private final AuthenticationMapper authenticationMapper;

    // 실제 Gmail SMTP 메일 발송 서비스
    private final MailSendService mailSendService;

    @Value("${app.mail.auth-expire-minutes}")
    private long authExpireMinutes;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    @Transactional
    public void sendVerificationCode(String email) {
        log.info("sendVerificationCode : "+email);

        // 1. 이메일 정규화
        String normalizedEmail = email.trim().toLowerCase();

        // 2. 이미 가입된 이메일 차단
        boolean result = authenticationMapper.existEmail(email);
        if (result) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);
        }

        // 3. 기존 인증 정보 조회 : 같은 이메일로 이전에 발송한 인증 이력이 있는지 확인
        EmailVerification existing = emailVerificationMapper.findTopByEmailOrderByCreatedAtDesc(normalizedEmail);

        // 4. 재발송 제한 : 최근 요청 후 30초 이내 재발송 금지
        if (existing != null && existing.getCreatedAt() != null) {
            LocalDateTime resendAvailableTime = existing.getCreatedAt().plusSeconds(30);
            if (LocalDateTime.now().isBefore(resendAvailableTime)) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
            }
        }

        // 5. 인증번호 / 만료시간 생성 : 현재 시간 기준 authExpireMinutes 만큼 유효시간 부여
        String authCode = generateAuthCode();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(authExpireMinutes);

        // 6. 인증 데이터 저장
        if (existing != null) {
            // 기존 인증 이력이 있으면 새 인증번호와 만료시간으로 갱신
            existing.refreshCode(authCode, expiredAt);
            // DB update 수행
            emailVerificationMapper.updateEmailVerification(existing);
        } else {
            // 기존 인증 이력이 없으면 새 객체 생성
            EmailVerification verification = new EmailVerification(normalizedEmail, authCode, expiredAt);
            // DB insert 수행
            emailVerificationMapper.insertEmailVerification(verification);
        }

        // 7. 메일 발송
        // - DB에 저장한 인증번호를 실제 메일로 발송
        mailSendService.sendAuthCodeMail(normalizedEmail, authCode);
    }

    /**
     * 인증번호 검증
     * @param email
     * @param authCode
     */
    public void verifyCode(String email, String authCode) {
        // 1. 이메일 정규화
        String normalizedEmail = email.trim().toLowerCase();

        // 2. 최근 인증정보 조회
        // - 해당 이메일의 가장 최근 인증 이력 1건 조회
        EmailVerification verification = emailVerificationMapper.findTopByEmailOrderByCreatedAtDesc(normalizedEmail);

        // 3. 인증 이력이 없으면 예외
        // - 이메일 발송 기록이 없으면 검증 자체가 불가능
        if (verification == null) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_CODE);
        }

        // 4. 만료되었으면 예외
        // - 인증코드 유효 시간이 지났으면 실패 처리
        if (verification.isExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_AUTH_CODE);
        }

        // 5. 인증번호가 다르면 예외
        // - 사용자가 입력한 인증번호와 DB에 저장된 인증번호 비교
        if (!verification.getAuthCode().equals(authCode.trim())) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_CODE);
        }

        // 6. 인증 성공 상태 반영
        // - VERIFIED = 'Y'
        // - VERIFIED_AT = 현재 시각
        verification.verify();

        // 7. DB 업데이트
        emailVerificationMapper.updateEmailVerification(verification);
    }

    /**
     * 회원가입 전에 이메일 인증 완료 여부 확인
     *
     * 목적:
     * - 프론트에서 인증 완료 상태를 조작하더라도
     *   백엔드에서 최종적으로 한 번 더 검증하기 위함
     *
     *   @param email
     */
    @Transactional(readOnly = true)
    public void validateVerifiedEmail(String email) {
        // 1. 이메일 정규화
        String normalizedEmail = email.trim().toLowerCase();

        // 2. 최근 인증 이력 조회
        EmailVerification verification =
                emailVerificationMapper.findTopByEmailOrderByCreatedAtDesc(normalizedEmail);

        // 3. 인증 이력이 없으면 회원가입 불가
        // - 인증 절차를 수행하지 않은 경우
        if (verification == null) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 4. 인증 완료 상태가 아니거나 만료되었으면 회원가입 불가
        // - 인증번호 입력을 하지 않았거나
        // - 인증 후 시간이 지나 만료된 경우
        if (!verification.isVerificationCompleted() || verification.isExpired()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }


    // 100000 ~ 999999 범위의 6자리 숫자 생성
    private String generateAuthCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
