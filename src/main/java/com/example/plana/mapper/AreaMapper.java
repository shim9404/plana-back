package com.example.plana.mapper;

import com.example.plana.dto.area.read.AreaForBookmarkResponse;
import com.example.plana.dto.area.read.AreaReadRequest;
import com.example.plana.model.Area;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AreaMapper {
    List<Area> readArea(@Param("regionId") String regionId);
    List<Area> readAreaByZdoCode(@Param("zdoCode") String zdoCode);
    Area readAreaForBookmark(String bookmarkId);
    void createArea(Map<String, Object> params);
    String readAreaIdByPlaceId(@Param("placeId") String placeId);
}
