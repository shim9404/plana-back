package com.example.plana.mapper;

import com.example.plana.model.EmailVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailVerificationMapper {
    /**
     * 이메일 기준 가장 최근 인증 정보 1건 조회
     *
     * @param email 이메일
     * @return 최근 인증 정보, 없으면 null
     */
    EmailVerification findTopByEmailOrderByCreatedAtDesc(@Param("email") String email);

    /**
     * 이메일 인증 정보 신규 저장
     *
     * @param emailVerification 이메일 인증 정보
     * @return 처리 건수
     */
    int insertEmailVerification(EmailVerification emailVerification);

    /**
     * 이메일 인증 정보 수정
     *
     * @param emailVerification 이메일 인증 정보
     * @return 처리 건수
     */
    int updateEmailVerification(EmailVerification emailVerification);
}
