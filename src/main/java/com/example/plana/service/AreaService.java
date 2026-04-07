package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.dto.area.read.*;
import com.example.plana.mapper.AreaMapper;
import com.example.plana.mapper.RegionMapper;
import com.example.plana.model.Area;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class AreaService {
    private final AreaMapper areaMapper;
    private final RegionMapper regionMapper;

    /**
     * 행정구역id 또는 시도code로 area table에 저장된 데이터 불러오기
     * @param regionId 행정구역 id - regionTable pk
     * @param zdoCode 시도 code - regionTable
     * @return
     */
    public AreaReadResponse getArea(String regionId, Integer zdoCode){

        // 존재하지 않는 지역입니다.
        if (regionId != null && regionMapper.checkRegionExists(regionId) == 0){
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        // 존재하지 않는 지역입니다.
        if (zdoCode != null && regionMapper.checkZdoExists(zdoCode) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        List<Area> areas = areaMapper.readArea(regionId, zdoCode);

        // getSearchType에 따라 그룹화
        Map<String, List<Area>> groupedMap = areas.stream()
                .collect(Collectors.groupingBy(Area::getSearchType));

        AreaReadResponse response = new AreaReadResponse();
        response.setRegionId(regionId);

        // 각 타입별로 변환해서 세팅
        AreaTypeResponse place = createTypeResponse("PLACE", groupedMap.get("PLACE"));
        AreaTypeResponse spot = createTypeResponse("SPOT",groupedMap.get("SPOT"));
        AreaTypeResponse food = createTypeResponse("FOOD",groupedMap.get("FOOD"));

        return new AreaReadResponse(areas.size(), regionId, place, spot, food);
    }

    /**
     * 특정 타입의 데이터를 받아 AreaTypeResponse 객체로 변환
     */
    private AreaTypeResponse createTypeResponse(String type, List<Area> areas) {
        if (areas == null || areas.isEmpty()) {
            return new AreaTypeResponse(type, 0, Collections.emptyList());
        }

        // Area(Model) -> AreaDetailResponse(DTO) 변환
        List<AreaDetailResponse> detailList = areas.stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        return new AreaTypeResponse(type, detailList.size(), detailList);
    }

    /**
     * Area Data를 AreaDetailRespose데이터로 가공하여 반환
     * @param area
     * @return AreaDetailResponse
     */
    private AreaDetailResponse toDetailResponse(Area area){

        AreaDetailResponse areaDetail = new AreaDetailResponse();

        areaDetail.setAreaId(area.getAreaId());
        areaDetail.setName(area.getName());

        MapPos mapPos = new MapPos();
        mapPos.setX(area.getMapX());
        mapPos.setY(area.getMapY());

        areaDetail.setMapPos(mapPos);

        areaDetail.setCategory(area.getCategory());
        areaDetail.setAddress(area.getAddress());
        areaDetail.setRoadAddress(area.getRoadAddress());
        areaDetail.setLink(area.getLink());
        areaDetail.setTelePhone(area.getTelePhone());
        areaDetail.setDescription(area.getDescription());
        areaDetail.setBookmarkCount(area.getBookmarkCount());
        areaDetail.setCreateDate(area.getCreateDate());
        areaDetail.setLatestDate(area.getLatestDate());
        areaDetail.setStatus(area.getStatus());

        return areaDetail;
    }
}
