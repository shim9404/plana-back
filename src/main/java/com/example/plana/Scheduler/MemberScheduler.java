package com.example.plana.Scheduler;

import com.example.plana.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberScheduler {
    private final MemberService memberService;

    // 회원 & 여행 정보 삭제
    // 매일 오전 10시 자동 실행 -> 30일 지난 경우(member.xml)
    @Scheduled(cron = "0 00 10 * * ?") //   -> 초 분 시 일 월 요일
    public void autoDelete() {
        System.out.println("자동 삭제 실행 시작");
        memberService.deleteOldMembers();
        memberService.deleteOldTrips();
        System.out.println("자동 삭제 실행 완료");
    }
}
