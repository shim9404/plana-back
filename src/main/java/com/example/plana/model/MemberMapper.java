package com.example.plana.model;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
    // 닉네임 중복 체크
    boolean existNickname(@Param("nickname") String nickname);
}
