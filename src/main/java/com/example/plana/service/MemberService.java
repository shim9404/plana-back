package com.example.plana.service;

import com.example.plana.dto.member.read.MemberReadResponse;
import com.example.plana.dto.member.update.MemberPwUpdateRequest;
import com.example.plana.dto.member.update.MemberStatusRequest;
import com.example.plana.dto.member.update.MemberUpdateRequest;
import com.example.plana.mapper.MemberMapper;
import com.example.plana.model.MemberVerify;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;

    // 닉네임 중복 체크
    public boolean existNickname(String nickname) {
        return memberMapper.existNickname(nickname);
    }

    // 회원 정보 호출
    public MemberReadResponse readMember(String memberId) {
        return memberMapper.readMember(memberId);
    }

    // 회원 정보 수정
    public void updateMember(String memberId, MemberUpdateRequest memberUpdateRequest) {
        memberMapper.updateMember(memberId, memberUpdateRequest);
    }

    // 현재 비밀번호 일치 여부 확인
    public boolean checkPassword(String memberId, String currentPassword) {
        // DB에서 암호화된 비밀번호 가져오기
        String encodedPassword = memberMapper.checkPassword(memberId);

        // 비밀번호 일치 여부 확인(true: 일치 / false: 비일치)
        //  - 같은 비밀번호여도 암호화할 때마다 다른 암호화 결과를 생성됨
        //   -> PasswordEncoder + matches: 내부적으로 salt, 해시값 비교
        // TODO: 비교(match) -> 추후 암호화 기능 제작할 경우, 수정 예정
        //  EX) passwordEncoder.matches(dto.getCurrentPassword(), encodedPassword))
        // 비교(equal) -> 암호화 전 상태로 임의 비교
        boolean check = encodedPassword.equals(currentPassword);
        return check;
    }

    // 새 비밀번호 변경
    public void updatePassword(String memberId, String newPassword) {
        // TODO: 새 비밀번호 암호화 -> 추후 암호화 기능 제작할 경우, 수정 예정

        // 새 비밀번호로 변경
        memberMapper.updatePassword(memberId, newPassword);
    }

    // 회원 정보 일치 여부 확인
    public boolean checkMember(String memberId, MemberStatusRequest memberStatusRequest) {
        MemberVerify memberVerify = memberMapper.checkMember(memberId);
        boolean check = false;
        // 이메일 일치 확인
        check = memberVerify.getEmail().equals(memberStatusRequest.getEmail());
        // 이름 일치 확인
        check = memberVerify.getName().equals(memberStatusRequest.getName());
        // 비밀번호 일치 확인
        //TODO: 비교(match) -> 추후 암호화 기능 제작할 경우, 수정 예정
        check = memberVerify.getPassword().equals(memberStatusRequest.getPassword());

        return check;
    }

    //  회원 정보 상태 변경(삭제)
    public void updateMemberStatus(String memberId) {
        memberMapper.updateMemberStatus(memberId);
    }

    // 회원 정보 삭제(자동 실행)
    public void deleteOldMembers() {
        memberMapper.deleteOldMembers();
    }
}
