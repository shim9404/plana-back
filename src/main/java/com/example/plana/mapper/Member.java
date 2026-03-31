package com.example.plana.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface Member {
    // 닉네임 중복 체크
    boolean existNickname(@Param("nickname") String nickname);
}
