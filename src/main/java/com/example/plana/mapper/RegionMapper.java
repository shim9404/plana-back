package com.example.plana.mapper;

import com.example.plana.model.Region;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegionMapper {
    List<Region> readRegion();
    int checkRegionExists(@Param("regionId") String regionId);
    int checkZdoExists(@Param("zdoCode") Integer zdoCode);
}
