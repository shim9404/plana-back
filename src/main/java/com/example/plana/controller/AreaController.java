package com.example.plana.controller;


import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.area.read.AreaReadResponse;
import com.example.plana.dto.area.read.AreaTypePageResponse;
import com.example.plana.dto.area.read.PlaceReadPageResponse;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.service.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Area API", description = "장소 API 목록")
public class AreaController {
    private final AreaService areaService;

    /**
     * 전달한 파라미터에 맞는 장소 DB에서 반환
     * @param regionId 행정구역 코드
     * @return ResponseBody.data : AreaReadResponse
     */
    @GetMapping
    @Operation(summary = "근처 장소 검색(장소 DB)", description = "근처 장소(DB), 관광지, 맛집 등 장소 목록을 조회한다.")
    @Parameters({ @Parameter(name = "regionId", description = "행정구역 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S001] 조회에 성공하였습니다.")
    public ResponseEntity<ResponseBody<AreaReadResponse>> getArea(@RequestParam(required = false) String regionId){
        AreaReadResponse data = areaService.getArea(regionId);

        return ResponseEntity.ok(
                    ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }

    /**
     * 특정 타입 추가 페이지 로드
     * GET /area/page?regionId=xxx&searchType=PLACE&page=2&size=20
     */
    @GetMapping("/page")
    @Operation(summary = "근처 장소 검색", description = "특정 searchType의 페이지를 추가로 불러온다.")
    @Parameters({
            @Parameter(name = "regionId", description = "행정구역 ID", required = true),
            @Parameter(name = "searchType", description = "검색 타입", required = true),
            @Parameter(name = "keyword", description = "키워드", required = true),
            @Parameter(name = "page", description = "불러올 페이지 번호 (기본값 = 1)", required = true),
            @Parameter(name = "size", description = "한 페이지에 들어갈 장소 수 (기본값 = 20)", required = true)
    })
    @ApiResponse(responseCode = "200", description = "[S001] 조회에 성공하였습니다.")
    public ResponseEntity<ResponseBody<AreaTypePageResponse>> getAreaByType(
            @RequestParam(required = false) String regionId,
            @RequestParam String searchType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "") String keyword) {

        log.info("keyword:: "+keyword);
        log.info(keyword);
        AreaTypePageResponse data = areaService.getAreaByType(regionId, searchType, page, size, keyword);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }

    /**
     *  getPlace(): 근처 장소 검색(카카오 API)
     *   -> readPlace(): 키워드로 장소 검색(장소 데이터)
     * @param keyword // 검색 키워드
     * @param mapX   // 좌표(X)
     * @param mapY,  // 좌표(Y)
     * @return ResponseBody.data : List<PlaceReadResponse>
     */
    @GetMapping("/place")
    @Operation(summary = "근처 장소 검색(카카오 API)", description = "검색 API로 키워드(기본값 = 음식점(FD6))와 위도,경도를 받아서 정보를 반환한다.")
    @Parameters({
            @Parameter(name = "keyword", description = "키워드", required = true),
            @Parameter(name = "mapX", description = "경도", required = true),
            @Parameter(name = "mapY", description = "위도", required = true),
            @Parameter(name = "page", description = "불러올 페이지 번호 (기본값 = 1)", required = true),
            @Parameter(name = "size", description = "한 페이지에 들어갈 장소 수 (기본값 = 15)", required = true)
    })
    @ApiResponse(responseCode = "200", description = "[S001] 조회에 성공하였습니다.")
    public ResponseEntity<ResponseBody<PlaceReadPageResponse>> getPlace(
            @RequestParam String keyword,
            @RequestParam double mapX,
            @RequestParam double mapY,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size) {

        PlaceReadPageResponse data = areaService.readPlace(keyword, mapX, mapY, page, size);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }
}
