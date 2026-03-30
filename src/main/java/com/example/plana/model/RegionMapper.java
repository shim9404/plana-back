package com.example.plana.model;

import com.example.plana.dto.RegionDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface RegionMapper {
    List<RegionDto> selectAllRegions();
}