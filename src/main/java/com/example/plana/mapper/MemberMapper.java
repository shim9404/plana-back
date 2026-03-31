package com.example.plana.mapper;

import com.example.plana.dto.member.read.MemberReadResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
    // 닉네임 중복 체크
    boolean existNickname(@Param("nickname") String nickname);

    // 회원 정보 호출
    MemberReadResponse readMember(@Param("memberId") String memberId);
}
