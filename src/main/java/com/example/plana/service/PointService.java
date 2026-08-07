package com.example.plana.service;

import com.example.plana.auth.Role;
import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.dto.point.read.PointReadResponse;
import com.example.plana.dto.point.read.PointResponse;
import com.example.plana.dto.trip.read.TripResponse;
import com.example.plana.mapper.MemberMapper;
import com.example.plana.mapper.PointMapper;
import com.example.plana.mapper.TripMapper;
import com.example.plana.model.PointSave;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class PointService {
    private final PointMapper pointMapper;
    private final MemberMapper memberMapper;
    private final TripMapper tripMapper;

    /**
     * 권한 체크
     * 데이터 CRUD를 요청한 id와 현재 로그인한 id를 비교하는 함수
     * @param tokenMemberId 토큰에서 추출한 사용자 id
     * @param pathMemberId url에서 추출한 사용자 id
     */
    public void validateOwner(String tokenMemberId, String pathMemberId, Role role) {
        log.info("pathId:: ",pathMemberId);
        log.info("tokenMemberId:: ",tokenMemberId);
        log.info("접근 권한 체크 발생");
        if (role == Role.ADMIN) return;;

        if (!tokenMemberId.equals(pathMemberId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    // 포인트 정보 호출
    public PointReadResponse readPoint(String tokenMemberId, String pathMemberId, Role role) {
        validateOwner(tokenMemberId, pathMemberId, role);

        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (memberMapper.readMember(tokenMemberId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<PointResponse> points = pointMapper.readPoint(tokenMemberId);

        int remain = 0;

        for (PointResponse point : points) {

            if ("EARN".equals(point.getType())) {
                remain += point.getAmount();
            } else if ("USE".equals(point.getType())
                    || "EXPIRE".equals(point.getType())) {
                remain -= point.getAmount();
            }

            point.setRemain(remain);
        }

        // 포인트 정보 호출
        return new PointReadResponse(tokenMemberId, points);
    }

    // 포인트 이벤트
    public enum PointEvent {
        SIGNUP("회원가입 축하 포인트"),
        TRIP_CREATE("[ %s ] 생성"),
        TRIP_SHARE("[ %s ] 공유"),
        POINT_EXPIRE("%s - 포인트 만료(60일)"),
        TRIP_CREATE_DELETE("%s - 삭제(24시간 이내)"),
        TRIP_SHARE_INACTIVE("%s - 비활성화(24시간 이내)"),
        POINT_USE_SLOT("여행 계획 생성 슬롯 추가");

        private final String content;

        PointEvent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    // 포인트 사용 [여행 슬롯 추가]
    public void createPointUse(String tokenMemberId, String pathMemberId, Role role, String event) {
        validateOwner(tokenMemberId, pathMemberId, role);

        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (memberMapper.readMember(tokenMemberId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // event 분기 처리
        // 여행 계획 생성 슬롯 수 추가
        if (event.equals("POINT_USE_SLOT")) {
            /*
            // 여행 슬롯 추가

            int resultUpdate = pointMapper.updatetripSlotCount(tokenMemberId);

            // 여행 슬롯 추가 실패
            if (resultUpdate != 1){
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            */

            // 포인트 목록 등록
            PointSave pointSave = new PointSave();

            pointSave.setMemberId(tokenMemberId);
            pointSave.setTripId(null);
            pointSave.setContent(String.format(PointEvent.POINT_USE_SLOT.getContent()));
            pointSave.setType("USE");
            pointSave.setEvent("POINT_USE_SLOT");
            pointSave.setAmount(1000);
            pointSave.setOriginPointId(null);

            int resultSave = pointMapper.createPoint(pointSave);

            // 포인트 등록 실패
            if (resultSave != 1){
                throw new BusinessException(ErrorCode.DATABASE_ERROR);
            }
        }

    }

    // 포인트 적립 [회원가입]
    public void createPointEarnSign(String memberId) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        PointSave pointSave = new PointSave();

        pointSave.setMemberId(memberId);
        pointSave.setTripId(null);
        pointSave.setContent(String.format(PointEvent.SIGNUP.getContent()));
        pointSave.setType("EARN");
        pointSave.setEvent("SIGNUP");
        pointSave.setAmount(1000);
        pointSave.setOriginPointId(null);

        int result = pointMapper.createPoint(pointSave);

        // 포인트 등록 실패
        if (result != 1){
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }
    }

    // 포인트 적립 [여행 계획 생성]
    public void createPointEarnTrip(String memberId, String tripId, String name) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        PointSave pointSave = new PointSave();

        pointSave.setMemberId(memberId);
        pointSave.setTripId(tripId);
        pointSave.setContent(String.format(PointEvent.TRIP_CREATE.getContent(), name));
        pointSave.setType("EARN");
        pointSave.setEvent("TRIP_CREATE");
        pointSave.setAmount(500);
        pointSave.setOriginPointId(null);


        int result = pointMapper.createPoint(pointSave);

        // 포인트 등록 실패
        if (result != 1){
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }
    }

    // 포인트 적립 수정(여행명 변경) [여행 계획 생성]
    public void updatePointEarnTrip(String memberId, String tripId, String name) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Map<String, Object> pointParams = new HashMap<>();

        pointParams.put("memberId", memberId);
        pointParams.put("tripId", tripId);
        pointParams.put("content", String.format(PointEvent.TRIP_CREATE.getContent(), name));

        int result = pointMapper.updatePoint(pointParams);

        // 포인트 수정 실패
        if (result != 1){
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }
    }

    // 포인트 적립 [여행 계획 공유]
    public void createPointEarnShare(String memberId, String tripId) {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        PointSave pointSave = new PointSave();

        pointSave.setMemberId(memberId);
        pointSave.setTripId(tripId);
        TripResponse trip = tripMapper.readTrip(tripId);
        pointSave.setContent(String.format(PointEvent.TRIP_SHARE.getContent(), trip.getName()));
        pointSave.setType("EARN");
        pointSave.setEvent("TRIP_SHARE");
        pointSave.setAmount(500);
        pointSave.setOriginPointId(null);


        int result = pointMapper.createPoint(pointSave);

        // 포인트 등록 실패
        if (result != 1){
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }
    }

    // 포인트 만료 [60일 경과]
    public void createPointExpire() {
        List<PointSave> pointList = pointMapper.readExpirePoint();

        for (PointSave point : pointList) {

            PointSave pointSave = new PointSave();

            pointSave.setMemberId(point.getMemberId());
            pointSave.setTripId(point.getTripId());
            pointSave.setContent(String.format(PointEvent.POINT_EXPIRE.getContent(), point.getContent()));
            pointSave.setType("EXPIRE");
            pointSave.setEvent("POINT_EXPIRE");
            pointSave.setAmount(point.getAmount());
            pointSave.setOriginPointId(point.getPointId());

            int result = pointMapper.createPoint(pointSave);

            if (result != 1) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    // 포인트 만료 [24시간 내 여행 계획 삭제]
    public void createPointExpireTrip(String memberId, String tripId) {
        PointSave point = pointMapper.readExpirePointTrip(memberId, tripId, "TRIP_CREATE");

        if (point != null) {
            PointSave pointSave = new PointSave();

            pointSave.setMemberId(point.getMemberId());
            pointSave.setTripId(point.getTripId());
            pointSave.setContent(String.format(PointEvent.TRIP_CREATE_DELETE.getContent(), point.getContent()));
            pointSave.setType("EXPIRE");
            pointSave.setEvent("TRIP_CREATE_DELETE");
            pointSave.setAmount(point.getAmount());
            pointSave.setOriginPointId(point.getPointId());

            int result = pointMapper.createPoint(pointSave);

            if (result != 1) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    // 포인트 만료 [24시간 내 여행 공유 비활성화]
    public void createPointExpireShare(String memberId, String tripId) {
        PointSave point = pointMapper.readExpirePointShare(memberId, tripId, "TRIP_SHARE");

        if (point != null) {
            PointSave pointSave = new PointSave();

            pointSave.setMemberId(point.getMemberId());
            pointSave.setTripId(point.getTripId());
            pointSave.setContent(String.format(PointEvent.TRIP_SHARE_INACTIVE.getContent(), point.getContent()));
            pointSave.setType("EXPIRE");
            pointSave.setEvent("TRIP_SHARE_INACTIVE");
            pointSave.setAmount(point.getAmount());
            pointSave.setOriginPointId(point.getPointId());

            int result = pointMapper.createPoint(pointSave);

            if (result != 1) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR);
            }
        }
    }


}
