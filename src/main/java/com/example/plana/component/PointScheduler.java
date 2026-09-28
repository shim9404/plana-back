package com.example.plana.component;

import com.example.plana.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointScheduler {
    private final PointService pointService;


    // 포인트 만료 삭제
    // 매일 자정에 자동 실행
    @Scheduled(cron = "0 0 0 * * ?") //   -> 초 분 시 일 월 요일
    public void autoExpire() {
        System.out.println("포인트 만료 처리 시작");
        // 포인트 만료 [60일 경과]
        pointService.createPointExpire();
        System.out.println("포인트 만료 처리 완료");
    }
}
