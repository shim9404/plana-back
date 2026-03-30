package com.example.plana.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.plana.service.MemberService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/members/")
public class MemberController {
    private final MemberService memberService;

    /** DEV-43
     * findPassword(): 닉네임 중복 체크 호출 함수
     *  -> existNickname(): 닉네임 중복 체크
     * @param nickname        // 새 닉네임
     * @return nicknameDupli  // 닉네임 중복 결과(TRUE: 중복 O/ FALSE: 중복 X)
     */
    @GetMapping("nickname/check")
    public boolean findPassword(@RequestParam("nickname") String nickname) {
        boolean nicknameDupli = memberService.existNickname(nickname);
        return nicknameDupli;
    }

}
