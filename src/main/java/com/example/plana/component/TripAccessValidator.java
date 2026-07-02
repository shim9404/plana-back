package com.example.plana.component;

import com.example.plana.common.constants.TripConstatnts;
import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.mapper.TripAccessMapper;
import com.example.plana.mapper.TripMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TripAccessValidator {
    private final TripMapper tripMapper;
    private final TripAccessMapper tripAccessMapper;

    /**
     * 여행 소유자 조회
     * @param tripId 여행 ID
     */
    private String getOwner(String tripId) {
        String ownerId;
        try {
            ownerId = tripAccessMapper.readTripOwner(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_READ_FAILED);
        }
        if (ownerId == null) {
            throw new BusinessException(ErrorCode.TRIP_NOT_FOUND);
        }
        return ownerId;
    }

    /**
     * 소유자 여부 검증
     * OWNER만 허용 (초대 발송, 토큰 발급, 여행 삭제 등)
     * CRUD를 요청한 TripId에서 memberId를 추출하고 현재 로그인한 id를 비교하는 함수
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     */
    public void validateOwner(String tripId, String memberId) {
        String tripOwner = getOwner(tripId);
        if (!tripOwner.equals(memberId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    /**
     * 편집 권한 검증
     * OWNER 또는 EDITOR만 허용 (스케줄 추가/수정/삭제 등)
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     */
    public void validateEditor(String tripId, String memberId) {
        String role = getAccessRole(tripId, memberId);

        if (role == null) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
        if (role.equals("VIEWER")) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }


    /**
     * 접근 권한 검증
     * OWNER, EDITOR, VIEWER 모두 허용 (여행 조회 등)
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     */
    public void validateAccess(String tripId, String memberId) {
        String role = getAccessRole(tripId, memberId);

        if (role == null) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    /**
     * 현재 멤버의 권한 레벨 반환
     * 권한 없으면 null 반환
     * @param tripId 여행 ID
     * @param memberId 사용자 ID
     */
    public String getAccessRole(String tripId, String memberId) {
        // 소유자 여부 먼저 확인
        String ownerId = getOwner(tripId);
        if (ownerId.equals(memberId)) {
            return "OWNER";
        }

        // TRIP_MEMBER에서 초대된 멤버 권한 조회
        return tripAccessMapper.getMemberRole(tripId, memberId);
    }


    /**
     * 여행 공개 여부 반환
     * @param tripId 여행 ID
     * @return 공개 여부 (true: 공개, false: 비공개)
     */
    public boolean getIsPublic(String tripId) {
        String isPublic = tripMapper.readTripIsPublic(tripId);
        return TripConstatnts.IS_PUBLIC_YES.equals(isPublic);
    }
}
