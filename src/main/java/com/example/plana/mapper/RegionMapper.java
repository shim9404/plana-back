package com.example.plana.mapper;

import com.example.plana.dto.region.read.RegionReadResponse;
import com.example.plana.model.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegionMapper {
    List<Region> readRegion();
    int checkRegionExists(@Param("regionId") String regionId);
    int checkZdoExists(@Param("zdoCode") Integer zdoCode);

    // 지역 간단 정보(좌표 + 이름) 호출
    RegionReadResponse readRegionById(@Param("regionId") String regionId);
}
