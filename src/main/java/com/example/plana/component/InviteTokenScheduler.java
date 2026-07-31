package com.example.plana.component;

import com.example.plana.service.TripInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InviteTokenScheduler {

    private final TripInviteService tripInviteService;

    // 초대 토큰 일괄 삭제
    // 매일 오전 10시 자동 실행 -> 3일 지난 경우(trip-member.xml)
    @Scheduled(cron = "0 00 10 * * ?") //   -> 초 분 시 일 월 요일
    public void autoDelete() {
        System.out.println("여행 초대 토큰 자동 삭제 실행 시작");
        tripInviteService.deleteExpiredInvitations();
        System.out.println("여행 초대 토큰 자동 삭제 실행 완료");
    }
}
