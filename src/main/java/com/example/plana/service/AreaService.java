package com.example.plana.service;

import com.example.plana.dto.area.read.*;
import com.example.plana.mapper.AreaMapper;
import com.example.plana.model.Area;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AreaService {
    private final AreaMapper areaMapper;

    public AreaReadResponse getArea(String regionId, String zdo){
        List<Area> areas = areaMapper.readArea(regionId, zdo);
        log.info(areas);

        List<AreaDetailResponse> places = new ArrayList<>();
        List<AreaDetailResponse> spots = new ArrayList<>();
        List<AreaDetailResponse> foods = new ArrayList<>();

        // 필요한 데이터 변환
        for (Area area : areas) {
            // 파라미터로 넘어온 regionId와 일치하지 않으면 추가 안 함 (한 번 더 확인용)
//            if (!regionId.isEmpty()){
//                if (!area.getRegionId().equals(regionId)) continue;;
//            }

            AreaDetailResponse areaDetail = new AreaDetailResponse();

            areaDetail.setAreaId(area.getAreaId());
            areaDetail.setName(area.getName());

            MapPos mapPos = new MapPos();
            mapPos.setX(area.getMapX());
            mapPos.setY(area.getMapY());
//            log.info("X: ", area.getMapX());
//            log.info("Y: ", area.getMapY());

            areaDetail.setMapPos(mapPos);

            log.info(mapPos);
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

            switch (area.getSearchType()){
                case "PLACE":
                    places.add(areaDetail);
                    break;
                case "SPOT":
                    spots.add(areaDetail);
                    break;
                case "FOOD":
                    foods.add(areaDetail);
                    break;
                default:
                    break;
            }
        }
        AreaTypeResponse placeResponse = new AreaTypeResponse();
        AreaTypeResponse spotResponse = new AreaTypeResponse();
        AreaTypeResponse foodResponse = new AreaTypeResponse();

        placeResponse.setAreaCount(places.size());
        spotResponse.setAreaCount(spots.size());
        foodResponse.setAreaCount(foods.size());

        placeResponse.setSearchType("PLACE");
        spotResponse.setSearchType("SPOT");
        foodResponse.setSearchType("FOOD");

        placeResponse.setAreas(places);
        spotResponse.setAreas(spots);
        foodResponse.setAreas(foods);

        AreaReadResponse areaReadResponse = new AreaReadResponse();
        areaReadResponse.setRegionId(regionId);
        areaReadResponse.setTotalCount(places.size()+spots.size()+foods.size());
        areaReadResponse.setPlace(placeResponse);
        areaReadResponse.setSpot(spotResponse);
        areaReadResponse.setFood(foodResponse);

        return areaReadResponse;
    }
}
