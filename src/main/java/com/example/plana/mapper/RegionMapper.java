package com.example.plana.mapper;

import com.example.plana.model.Region;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegionMapper {
    List<Region> readRegion();
}
