package com.example.plana.controller;

import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.region.read.RegionReadResponse;
import com.example.plana.dto.region.read.ZdoResponse;
import com.example.plana.model.Region;
import com.example.plana.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/regions")
public class RegionController {
    private final RegionService regionService;

    /**
     * 행정구역 정보 반환
     * @return ResponseBody.data : RegionResponse
     */
    @GetMapping
    public ResponseEntity<ResponseBody> region(){

        List<ZdoResponse> data = regionService.readRegion();

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, Map.of("regions",data))
        );
    }

    /** DEV-160
     * 지역 간단 정보 호출(여행 계획 페이지 진입)
     *  -> readRegionById(): 지역 간단 정보(좌표 + 이름) 호출
     * @param regionId // 지역 고유 ID
     * @return ResponseBody.data : RegionReadResponse
     */
    @GetMapping("/find")
    public ResponseEntity<ResponseBody> getRegionById(@RequestParam String regionId) {
        RegionReadResponse data = regionService.readRegionById(regionId);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, Map.of("regions", data))
        );
    }
}
