package com.example.plana.dto.member.create;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateResponse {
    private String memberId;
    /** 이메일 */
    private String email;
    /** 비밀번호 (암호화 저장) */
    private String password;
    /** 이름 */
    private String name;
    /** 닉네임 */
    private String nickname;
    /** 로그인 타입 (LOCAL, GOOGLE, KAKAO 등) */
    private String socialType;
    /** 권한 (USER, ADMIN 등) */
    private String role;
    /** 생성일 */
    private String createDate;
    /** 상태 (ACTIVE, INACTIVE, BLOCKED) */
    private String status;
}
