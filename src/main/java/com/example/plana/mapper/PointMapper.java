package com.example.plana.mapper;

import com.example.plana.dto.point.read.PointResponse;
import com.example.plana.model.PointSave;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PointMapper {
    // 포인트 정보 호출
    List<PointResponse> readPoint(@Param("memberId") String memberId);

    // 여행 슬롯 추가(포인트 사용)
    // TODO POINT-1: DB 연결 - 여행 슬롯 수 변경
    int updatetripSlotCount(@Param("memberId") String memberId);

    // 포인트 등록
    int createPoint(@Param("point") PointSave pointSave);

    // 포인트 수정
    int updatePoint(Map<String, Object> params);

    // 기간 만료(60일 경과)인 포인트 호출
    List<PointSave> readExpirePoint();

    // 여행 계획 삭제(24시간 내)인 포인트 호출
    PointSave readExpirePointTrip(@Param("memberId") String memberId, @Param("tripId") String tripId, @Param("event") String event);

    // 여행 공유 비활성화(24시간 내)인 포인트 호출
    PointSave readExpirePointShare(@Param("memberId") String memberId, @Param("tripId") String tripId, @Param("event") String event);

}
