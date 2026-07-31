package com.example.plana.mapper;

import com.example.plana.model.TripMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface TripMemberMapper {
    String getMemberIdByEmail(String email);
    boolean checkInvitationExists(Map<String, Object> params);
    void createInvitation(Map<String, Object> params);
    TripMember readInvitationByToken(String inviteToken);
    void updateInvitationAccept(Map<String, Object> params);
    void deleteExpiredInvitation();
    void deleteExpiredInvitations();
}
