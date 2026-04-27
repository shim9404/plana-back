package com.example.plana.service;


import com.example.plana.dto.area.read.MapPos;
import com.example.plana.dto.region.read.SiguResponse;
import com.example.plana.dto.region.read.ZdoResponse;
import com.example.plana.mapper.RegionMapper;
import com.example.plana.model.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class RegionService {
    private final RegionMapper regionMapper;

    /**
     * 지역 정보 불러오기
     * @return RegionResponse 시도리스트 -> 시군구 리스트 뎁스 구조로 반환
     */
    public List<ZdoResponse> readRegion(){

        // DB에서 지역 정보 불러오기
        List<Region> siguDatas = regionMapper.readRegion();

        Map<Integer, ZdoResponse> regionMap = new HashMap<>();

        for (Region siguData : siguDatas) {
            ZdoResponse zdo;
            List<SiguResponse> siguList;

            // regionMap에 등록한 값이아니라면 새로 추가
            if (!regionMap.containsKey(siguData.getZdoCode())) {
                zdo = new ZdoResponse();
                zdo.setZdoCode(siguData.getZdoCode());
                zdo.setZdoName(siguData.getZdoName());
                siguList = new ArrayList<>();
            } else {
                zdo = regionMap.get(siguData.getZdoCode());
                siguList = zdo.getSigus();
            }

            // 매핑
            SiguResponse sigu = new SiguResponse();

            sigu.setRegionId(siguData.getRegionId());
            sigu.setSiguCode(siguData.getSiguCode());
            sigu.setSiguName(siguData.getSiguName());
            MapPos mapPos = new MapPos();
            mapPos.setX(siguData.getMapX());
            mapPos.setY(siguData.getMapY());
            sigu.setMapPos(mapPos);
            sigu.setCreateDate(siguData.getCreateDate());
            sigu.setLatestDate(siguData.getLatestDate());
            sigu.setStatus(siguData.getStatus());

            siguList.add(sigu);
            zdo.setSigus(siguList);
            regionMap.put(zdo.getZdoCode(), zdo);
        }

        return new ArrayList<>(regionMap.values());
    }
}
