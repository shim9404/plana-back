package com.example.plana.mapper;

import com.example.plana.dto.area.read.AreaReadRequest;
import com.example.plana.model.Area;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AreaMapper {
    List<Area> readArea(@Param("regionId") String regionId, @Param("zdoCode") Integer zdoCode);
    void createArea(Map<String, Object> params);
}
