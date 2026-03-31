package com.example.plana.service;

import com.example.plana.dto.member.read.MemberReadResponse;
import com.example.plana.dto.member.update.MemberUpdateRequest;
import com.example.plana.mapper.MemberMapper;
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
}
