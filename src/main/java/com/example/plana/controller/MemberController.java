package com.example.plana.controller;

import com.example.plana.dto.common.ResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.plana.service.MemberService;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    /** DEV-43
     * dupliNickname(): 닉네임 중복 체크 호출 함수
     *  -> existNickname(): 닉네임 중복 체크(결과 - TRUE: 중복 O/ FALSE: 중복 X)
     * @param nickname        // 새 닉네임
     * @return ResponseBody.data : nicknameDupli
     */
    @GetMapping("/nickname/check")
    public ResponseEntity<ResponseBody> dupliNickname(@RequestParam("nickname") String nickname) {
        boolean nicknameDupli = memberService.existNickname(nickname);

        ResponseBody response = ResponseBody.builder()
                .success(true)
                .code(200)
                .message("OK")
                .data(Map.of("nicknameDupli", nicknameDupli))
                .build();

        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
