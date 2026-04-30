package com.example.plana.mapper;

import com.example.plana.dto.member.read.MemberReadResponse;
import com.example.plana.dto.member.read.MemberTripResponse;
import com.example.plana.dto.member.read.MemberTripTrashResponse;
import com.example.plana.dto.member.update.MemberUpdateRequest;
import com.example.plana.model.Member;
import com.example.plana.model.MemberSave;
import com.example.plana.model.MemberVerify;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MemberMapper {
    // 닉네임 중복 체크
    boolean existNickname(@Param("nickname") String nickname);

    // 회원 정보 호출
    MemberReadResponse readMember(@Param("memberId") String memberId);

    // 회원 정보 호출
    Member readMemberByEmail(@Param("email") String email);

    // 회원 정보 호출
    Member readMemberById(@Param("memberId") String memberId);

    // 회원 정보 수정
    void updateMember(@Param("memberId") String memberId, @Param("data") MemberUpdateRequest memberUpdateRequest);

    // 비밀번호 호출
    String checkPassword(@Param("memberId") String memberId);

    // 새 비밀번호 수정
    void updatePassword(@Param("memberId") String memberId, @Param("newPassword") String newPassword);

    // 회원 정보 일치 여부 확인
    MemberVerify checkMember(@Param("memberId") String memberId);

    // 회원 정보 삭제(자동 실행 - 30일 지난 경우 & STATUS: 'DELETED')
    void deleteOldMembers();

    // 회원 여행 목록 호출
    List<MemberTripResponse> readTripByMemberId(@Param("memberId") String memberId);

    int createMember(@Param("member") MemberSave memberSave);

    boolean existsEmail(String email);

    // INACTIVE(비활성)된 여행 정보 호출
    List<MemberTripTrashResponse> readTripTrash(@Param("memberId") String memberId);

    // 여행 정보 삭제(자동 실행 - 30일 지난 경우 & STATUS: ''INACTIVE')
    void deleteOldTrips();

    List<Map<String, Object>> memberList(Map<String, Object> map);

    int updateMemberRole (@Param("memberId") String memberId, @Param("role") String role);
    int updateMemberStatus (@Param("memberId") String memberId, @Param("status") String status);
}
