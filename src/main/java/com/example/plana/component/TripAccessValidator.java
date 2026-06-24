package com.example.plana.component;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.mapper.TripAccessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TripAccessValidator {

    private final TripAccessMapper tripAccessMapper;

    /**
     * 권한 체크
     * CRUD를 요청한 TripId에서 memberId를 추출하고 현재 로그인한 id를 비교하는 함수
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     */
    public void validateOwner(String tripId, String memberId) {
        String tripOwner;
        try {
            tripOwner = tripAccessMapper.readTripOwner(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_READ_FAILED);
        }
        if (tripOwner == null) {
            throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);
        }
        if (!tripOwner.equals(memberId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }
}
