package com.example.plana.mapper;

import com.example.plana.dto.member.read.MemberReadResponse;
import com.example.plana.dto.member.update.MemberUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
    // 닉네임 중복 체크
    boolean existNickname(@Param("nickname") String nickname);

    // 회원 정보 호출
    MemberReadResponse readMember(@Param("memberId") String memberId);

    // 회원 정보 수정
    void updateMember(@Param("memberId") String memberId, @Param("data") MemberUpdateRequest memberUpdateRequest);

    // 비밀번호 호출
    String checkPassword(@Param("memberId") String memberId);

    // 새 비밀번호 수정
    void updatePassword(@Param("memberId") String memberId, @Param("newPassword") String newPassword);
}
