package com.example.plana.service;

import com.example.plana.mapper.Member;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final Member member;

    // 닉네임 중복 체크
    public boolean existNickname(String nickname) {
        return member.existNickname(nickname);
    }
}
