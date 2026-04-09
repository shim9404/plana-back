package com.example.plana.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    /** 회원 ID (PK) */
    private String memberId;
    /** 이메일 */
    private String email;
    /** 비밀번호 (암호화 저장) */
    private String password;
    /** 이름 */
    private String name;
    /** 닉네임 */
    private String nickname;
    /** 프로필 이미지 URL */
    private String profileImage;
    /** 로그인 타입 (LOCAL, GOOGLE, KAKAO 등) */
    private String socialType;
    /** 권한 (USER, ADMIN 등) */
    private String role;
    /** Access Token */
    private String accessToken;
    /** Refresh Token */
    private String refreshToken;
    /** 생성일 */
    private String createDate;
    /** 최근 로그인 일시 */
    private String latestDate;
    /** 상태 (ACTIVE, INACTIVE, BLOCKED) */
    private String status;
}
