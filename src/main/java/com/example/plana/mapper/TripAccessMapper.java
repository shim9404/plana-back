package com.example.plana.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TripAccessMapper {
    // ── 여행 소유자 반환 ──────────────────────────
    String readTripOwner(String tripId);

    // ── 여행 접근 권한 반환 ────────────────────────
    String getMemberRole(String tripId, String memberId);

}
