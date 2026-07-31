package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


// 실제 메일 발송 서비스
@Service
@RequiredArgsConstructor
@Log4j2
public class MailSendService {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * 인증번호 메일 발송
     * @param toEmail 입력한 이메일
     * @param authCode 발급한 코드
     */
    public void sendAuthCodeMail(String toEmail, String authCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // 발신자
            message.setFrom(fromEmail);
            // 수신자
            message.setTo(toEmail);

            // 제목
            message.setSubject("[PLAN A] 이메일 인증번호 안내");

            // 본문
            message.setText(buildMailContent(authCode));

            javaMailSender.send(message);
        } catch (Exception e) {
            log.warn("메일 발송 실패: to={}, cause={}", toEmail, e.toString());
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }


    /***
     * 인증번호 메일 본문 생성
     * @param authCode 인증번호
     * @return String - 인증번호가 들어간 본문
     */
    private String buildMailContent(String authCode) {
        return """
                안녕하세요.
                                
                회원가입 이메일 인증번호를 안내드립니다.
                                
                인증번호: %s
                                
                본 인증번호는 5분간 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                """.formatted(authCode);
    }

    /**
     * 여행 계획 공동 작성 초대 메일 발송
     * @param toEmail 초대받는 사람 이메일
     * @param tripName 여행 이름
     * @param inviterName 초대한 사람 이름
     * @param inviteToken 초대 수락에 사용할 토큰
     */
    public void sendTripInviteMail(String toEmail, String tripName, String inviterName, String inviteToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[PLAN A] 여행 계획 초대 안내");
            message.setText(buildInviteMailContent(tripName, inviterName, inviteToken));

            javaMailSender.send(message);
        } catch (Exception e) {
            log.warn("초대 메일 발송 실패: to={}, cause={}", toEmail, e.toString());
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    /***
     * 여행 계획 공동 작성 초대 메일 본문 생성
     * @param inviterName 초대자 이름
     * @param inviteToken 초대 토큰(URL 작성용)
     * @return String - 인증번호가 들어간 본문
     */
    private String buildInviteMailContent(String tripName, String inviterName, String inviteToken) {
        String inviteUrl = frontendUrl + "/invite/accept?token=" + inviteToken;

        return """
            안녕하세요.

            %s 님이 '%s' 여행 계획에 회원님을 초대했습니다.

            아래 링크를 클릭하여 초대를 수락하고 함께 여행 계획을 작성해보세요!
            %s

            ○ 본 초대 링크는 3일간 유효합니다.
            ○ 본인이 요청하지 않았다면 이 메일을 무시해 주세요.
            """.formatted(inviterName, tripName, inviteUrl);
    }
}