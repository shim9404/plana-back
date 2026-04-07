package com.example.plana.controller;


import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.area.read.AreaReadRequest;
import com.example.plana.dto.area.read.AreaReadResponse;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/area")
public class AreaController {
    private final AreaService areaService;

    /**
     * 전달한 파라미터에 맞는 장소 DB에서 반환
     * @param regionId 행정구역 코드
     * @param zdoCode 도코드
     * @return ResponseBody.data : AreaReadResponse
     */
    @GetMapping
    public ResponseEntity<ResponseBody> getArea(@RequestParam(required = false) String regionId,
                                    @RequestParam(required = false) Integer zdoCode){

        AreaReadResponse data = areaService.getArea(regionId, zdoCode);

        return ResponseEntity.ok(
                    ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }
}
