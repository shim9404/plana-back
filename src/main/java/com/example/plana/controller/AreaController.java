package com.example.plana.controller;


import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.area.read.AreaReadResponse;
import com.example.plana.dto.area.read.AreaTypePageResponse;
import com.example.plana.dto.area.read.PlaceReadPageResponse;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.service.AreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/areas")
public class AreaController {
    private final AreaService areaService;

    /**
     * 전달한 파라미터에 맞는 장소 DB에서 반환
     * @param regionId 행정구역 코드
     * @return ResponseBody.data : AreaReadResponse
     */
    @GetMapping
    public ResponseEntity<ResponseBody> getArea(@RequestParam(required = false) String regionId){
        AreaReadResponse data = areaService.getArea(regionId);

        return ResponseEntity.ok(
                    ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }

    /**
     * 특정 타입 추가 페이지 로드
     * GET /area/page?regionId=xxx&searchType=PLACE&page=2&size=20
     */
    @GetMapping("/page")
    public ResponseEntity<ResponseBody> getAreaByType(
            @RequestParam(required = false) String regionId,
            @RequestParam String searchType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {


        AreaTypePageResponse data = areaService.getAreaByType(regionId, searchType, page, size);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
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
    public ResponseEntity<ResponseBody> getPlace(
            @RequestParam String keyword,
            @RequestParam double mapX,
            @RequestParam double mapY,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {

        PlaceReadPageResponse data = areaService.readPlace(keyword, mapX, mapY, page, size);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }
}
