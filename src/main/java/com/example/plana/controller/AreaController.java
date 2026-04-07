package com.example.plana.controller;


import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.area.read.AreaReadRequest;
import com.example.plana.dto.area.read.AreaReadResponse;
import com.example.plana.dto.area.read.PlaceReadResponse;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/areas")
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

    /** DEV-61
     *  getPlace(): 근처 장소 검색(카카오 API)
     *   -> readPlace(): 키워드로 장소 검색(장소 데이터)
     * @param keyword // 검색 키워드
     * @param mapX   // 좌표(X)
     * @param mapY,  // 좌표(Y)
     * @return ResponseBody.data : List<PlaceReadResponse>
     */
    @GetMapping("/place")
    public ResponseEntity<ResponseBody> getPlace(@RequestParam("keyword") String keyword, @RequestParam("mapX") double mapX, @RequestParam("mapY") double mapY) {
        List<PlaceReadResponse> data = areaService.readPlace(keyword, mapX, mapY);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, Map.of("place", data)));
    }
}
