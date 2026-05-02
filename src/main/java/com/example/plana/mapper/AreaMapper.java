package com.example.plana.mapper;

import com.example.plana.dto.area.read.AreaPageRequest;
import com.example.plana.model.Area;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AreaMapper {
    List<Area> readAreaByPage(AreaPageRequest request);
    int countAreaByType(AreaPageRequest request);
    List<Area> readAreaByZdoCode(AreaPageRequest request);
    int countAreaByZdoCode(AreaPageRequest request);
    Area readAreaForBookmark(String bookmarkId);
    void createArea(Map<String, Object> params);
    String readAreaIdByPlaceId(@Param("placeId") String placeId);
}
