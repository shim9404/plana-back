package com.example.plana.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TripAccessMapper {
    // ── 여행 소유자 반환 ──────────────────────────
    String readTripOwner(String tripId);


}
