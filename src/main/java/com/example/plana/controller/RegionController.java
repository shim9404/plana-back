package com.example.plana.controller;

import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.region.read.RegionReadResponse;
import com.example.plana.dto.region.read.RegionZdoListResponse;
import com.example.plana.dto.region.read.ZdoResponse;
import com.example.plana.model.Region;
import com.example.plana.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Region API", description = "행정 구역 API 목록")
public class RegionController {
    private final RegionService regionService;

    /**
     * 행정구역 정보 반환
     * @return ResponseBody.data : RegionResponse
     */
    @GetMapping
    @Operation(summary = "지역 정보 호출", description = "행정구역 정보(시/도, 시/군/구) 목록을 조회한다.")
    @ApiResponse(responseCode = "200", description = "[S001] 조회에 성공하였습니다.")
    public ResponseEntity<ResponseBody<RegionZdoListResponse>> region(){

        RegionZdoListResponse data = regionService.readRegion();

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, data)
        );
    }

    /** DEV-160
     * 지역 간단 정보 호출(여행 계획 페이지 진입)
     *  -> readRegionById(): 지역 간단 정보(좌표 + 이름) 호출
     * @param regionId // 지역 고유 ID
     * @return ResponseBody.data : RegionReadResponse
     */
    @GetMapping("/find")
    @Operation(summary = "지역 간단 정보 호출", description = "선택된 지역의 행정 구역 좌표 및 이름(시/도, 시/군/구)을 호출한다.")
    @Parameters({ @Parameter(name = "regionId", description = "행정 구역 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S001] 조회에 성공하였습니다.")
    public ResponseEntity<ResponseBody<RegionReadResponse>> getRegionById(@RequestParam String regionId) {
        RegionReadResponse data = regionService.readRegionById(regionId);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, data)
        );
    }
}
