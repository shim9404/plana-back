package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.common.utils.DateUtils;
import com.example.plana.component.TripAccessValidator;
import com.example.plana.dto.trip.invite.TripInviteAcceptRequest;
import com.example.plana.dto.trip.invite.TripInviteAcceptResponse;
import com.example.plana.dto.trip.invite.TripInviteRequest;
import com.example.plana.dto.trip.invite.TripInviteResponse;
import com.example.plana.mapper.MemberMapper;
import com.example.plana.mapper.TripMapper;
import com.example.plana.mapper.TripMemberMapper;
import com.example.plana.model.TripMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class TripInviteService {
    // 실제 Gmail SMTP 메일 발송 서비스
    private final MailSendService mailSendService;
    private final TripMapper tripMapper;
    private final TripMemberMapper tripMemberMapper;
    private final MemberMapper memberMapper;

    private final TripAccessValidator tripAccessValidator;

    /**
     * 여행 공동 작업 초대 발송
     * @param tripId 초대할 여행ID
     * @param inviterMemberId 초대자 멤버ID
     * @param request TripInviteRequest
     * @return TripInviteResponse
     */
    @Transactional
    public TripInviteResponse inviteMember(String tripId, String inviterMemberId, TripInviteRequest request) {

        // 1. 여행 소유자 검증
        tripAccessValidator.validateOwner(tripId, inviterMemberId);

        // 2. 중복 초대 확인
        Map<String, Object> existsParams = new HashMap<>();
        existsParams.put("tripId",       tripId);
        existsParams.put("invitedEmail", request.getInvitedEmail());

        boolean alreadySent;
        try {
            alreadySent = tripMemberMapper.checkInvitationExists(existsParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_READ_FAILED);
        }

        if (alreadySent) {
            throw new BusinessException(ErrorCode.INVITE_ALREADY_SENT);
        }

        // 3. 초대받는 이메일로 가입된 회원 조회 (없으면 null)
        String invitedMemberId = null;
        try {
            invitedMemberId = memberMapper.readMemberIdByEmail(request.getInvitedEmail());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 4. 초대 토큰 발급 및 TRIP_MEMBER INSERT
        String inviteToken = UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> inviteParams = new HashMap<>();
        inviteParams.put("tripMemberId", null);
        inviteParams.put("tripId",       tripId);
        inviteParams.put("memberId",     invitedMemberId);  // 가입되지 않은 유저의 경우 null
        inviteParams.put("invitedEmail", request.getInvitedEmail());
        inviteParams.put("inviteToken",  inviteToken);
        inviteParams.put("role",         request.getRole());

        try {
            tripMemberMapper.createInvitation(inviteParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVITE_SEND_FAILED);
        }

        // 5. 초대 메일 발송
        // 여행 이름 조회
        String tripName = "";
        try {
            tripName = tripMapper.readNameByTripId(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);
        }
        // 초대자명 조회
        String inviterName = "";
        try {
            inviterName = memberMapper.readNicknameByMemberId(inviterMemberId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        try {
            mailSendService.sendTripInviteMail(
                    request.getInvitedEmail(),
                    tripName,
                    inviterName,
                    inviteToken
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVITE_SEND_FAILED);
        }

        return TripInviteResponse.builder()
                .tripId(tripId)
                .invitedEmail(request.getInvitedEmail())
                .role(request.getRole())
                .build();
    }

    /**
     * 여행 공동 작업 초대 수락
     * @param memberId 수락한 멤버ID
     * @param request TripInviteAcceptRequest
     * @return TripInviteAcceptResponse
     */
    @Transactional
    public TripInviteAcceptResponse acceptInvitation(String memberId, TripInviteAcceptRequest request) {
        // 1. 토큰으로 초대 정보 조회
        TripMember invitation;
        try {
            invitation = tripMemberMapper.readInvitationByToken(request.getInviteToken());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_READ_FAILED);
        }

        if (invitation == null) {
            throw new BusinessException(ErrorCode.INVITE_TOKEN_NOT_FOUND);
        }

        // 2. 이미 수락된 초대인지 확인
        if (invitation.getJoinedAt() != null) {
            throw new BusinessException(ErrorCode.INVITE_ALREADY_ACCEPTED);
        }

        // 3. 만료 여부 확인 (3일)
        LocalDateTime invitedAt = DateUtils.parseDateTime(invitation.getInvitedAt());
        if (invitedAt.plusDays(3).isBefore(LocalDateTime.now())) {
            tripMemberMapper.deleteExpiredInvitation();
            throw new BusinessException(ErrorCode.INVITE_TOKEN_EXPIRED);
        }

        // 4. 이메일 일치 여부 확인
        String invitedEmail = invitation.getInvitedEmail();
        if (!invitedEmail.equals(request.getEmail())) {
            throw new BusinessException(ErrorCode.INVITE_EMAIL_MISMATCH);
        }

        // 5. 수락 처리
        Map<String, Object> acceptParams = new HashMap<>();
        acceptParams.put("inviteToken", request.getInviteToken());
        acceptParams.put("memberId",    memberId);

        try {
            tripMemberMapper.updateInvitationAccept(acceptParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_UPDATE_FAILED);
        }

        return TripInviteAcceptResponse.builder()
                .tripId(invitation.getTripId())
                .memberId(memberId)
                .role(invitation.getRole())
                .build();
    }

    /**
     * 만료 토큰 자동 삭제
     * 스케줄러 호출용 함수
     */
    public void deleteExpiredInvitations() {
        tripMemberMapper.deleteExpiredInvitations();
    }
}
