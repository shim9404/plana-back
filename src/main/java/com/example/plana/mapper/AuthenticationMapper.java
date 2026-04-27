package com.example.plana.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthenticationMapper {
    // 이메일 중복 체크
    boolean existEmail(@Param("email") String email);
}
