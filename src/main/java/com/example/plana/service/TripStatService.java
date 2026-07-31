package com.example.plana.service;

import com.example.plana.mapper.TripStatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripStatService {

    private final TripStatMapper tripStatMapper;

    /**
     * TRIP_STAT 갱신
     * TRIP_SCHEDULE 또는 BOOKMARK 변경 시 호출
     * 없으면 INSERT, 있으면 UPDATE
     * @param tripId 여행 ID
     */
    @Transactional
    public void refreshTripStat(String tripId) {
        if (tripStatMapper.checkTripStatExists(tripId) == 0) {
            tripStatMapper.createTripStat(tripId);
        } else {
            tripStatMapper.updateTripStat(tripId);
        }
    }
}