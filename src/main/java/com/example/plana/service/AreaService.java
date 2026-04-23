package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.config.KakaoConfig;
import com.example.plana.dto.area.create.AreaPlaceCreateRequest;
import com.example.plana.dto.area.read.*;
import com.example.plana.dto.bookmark.read.BookmarkResponse;
import com.example.plana.mapper.AreaMapper;
import com.example.plana.mapper.RegionMapper;
import com.example.plana.model.Area;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class AreaService {
    private final AreaMapper areaMapper;
    private final KakaoConfig kakaoConfig; // kakao apiKey
    private final RegionMapper regionMapper;

    /**
     * 행정구역id 또는 시도code로 area table에 저장된 데이터 불러오기
     * @param regionId 행정구역 id - regionTable pk
     * @return AreaReadResponse
     */
    public AreaReadResponse getArea(String regionId){
        List<Area> areas = null;

        // 존재하지 않는 지역입니다.
        if (regionId != null){ 
            if (regionMapper.checkRegionExists(regionId) == 0){
                log.info("존재하지 않는 지역입니다.");
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
            }

            String zdoCode = regionId.substring(0,2);
            String siguCode = regionId.substring(regionId.length()-3, regionId.length());

            if (siguCode.equals("000")){
                // zdocode를 가진 전체 area 반환
                areas = areaMapper.readAreaByZdoCode(zdoCode);
            }
            else {
                // 특정 sigu 반환
                areas = areaMapper.readArea(regionId);
            }
        }
        else {
            // Area 테이블 내 모든 정보 반환
            areas = areaMapper.readArea(null);
        }

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

    // 근처 장소 검색(API)
    public List<PlaceReadResponse> readPlace(String keyword, double mapX, double mapY) {
        // RestTemplate: RESTful API 웹 서비스와의 상호작용을 쉽게 외부 도메인에서 데이터를 가져오거나 전송할 때 사용
        RestTemplate restTemplate = new RestTemplate();

        // GET URL
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query="
                + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=20000" + "&sort=distance";

        // Header 부분(인증 키)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kakaoConfig.getClientId());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 근처 장소 검색
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class );

        // JSON -> MAP 변환
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result;
        // 검색 실패 시, error code 호출
        try {
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // documents 꺼내기
        List<Map<String, Object>> documents = (List<Map<String, Object>>) result.get("documents");

        // 필요한 값만 저장
        List<PlaceReadResponse> list = new ArrayList<>();

        for (Map<String, Object> doc: documents) {
            PlaceReadResponse placeReadResponse = new PlaceReadResponse();
            placeReadResponse.setSearchType("PLACE");
            // TODO: Bookmark 되어 있는지 확인하고 저장하는 코드 필요
            placeReadResponse.setBookmarkType("None");
            placeReadResponse.setName((String) doc.get("place_name"));
            MapPos mapPos = new MapPos();
            mapPos.setX(Double.parseDouble((String) doc.get("x")));
            mapPos.setY(Double.parseDouble((String) doc.get("y")));
            placeReadResponse.setMapPos(mapPos);
            if (doc.get("category_group_name") == null || doc.get("category_group_name") == "") {
                placeReadResponse.setCategory("기타");
            }
            else {
                placeReadResponse.setCategory((String) doc.get("category_group_name"));
            }
            placeReadResponse.setAddress((String) doc.get("address_name"));
            placeReadResponse.setRoadAddress((String) doc.get("road_address_name"));
            placeReadResponse.setLink((String) doc.get("place_url"));
            placeReadResponse.setTelePhone((String) doc.get("phone"));
            placeReadResponse.setDescription("카카오개발자센터_키워드로 장소 검색");

            list.add(placeReadResponse);
        }

        return list;
    }

    /**
     * 신규 근처 장소 등록
     * AREA DB에 존재하지 않는 근처 장소(PLACE)를 북마크한 경우 호출
     * @param request AreaPlaceCreateRequest
     * @return 등록 완료된 AREA ID
     */
    public String createNewPlaceAreaBeforeBookmark(AreaPlaceCreateRequest request) {
        Map<String, Object> areaParams = new HashMap<>();
        areaParams.put("regionId", request.getRegionId());
        areaParams.put("name", request.getName());
        areaParams.put("mapX", request.getMapPos().getX());
        areaParams.put("mapY", request.getMapPos().getY());
        areaParams.put("category", request.getCategory());
        areaParams.put("address", request.getAddress());
        areaParams.put("roadAddress", request.getRoadAddress());
        areaParams.put("link", request.getLink());
        areaParams.put("telephone", request.getTelephone());
        areaParams.put("description", request.getDescription());
        areaParams.put("areaId", null);

        log.info(areaParams);

        try {
            areaMapper.createArea(areaParams);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new BusinessException(ErrorCode.AREA_CREATE_FAILED);
        }

        return (String) areaParams.get("areaId");
    }

    /**
     * Area Data를 AreaForBookmarkResponse로 가공하여 반환
     * @param areaId 장소 ID
     * @return AreaForBookmarkResponse
     */
    public AreaForBookmarkResponse toBookmarkResponse(String areaId) {
        try {
            Area area = areaMapper.readAreaForBookmark(areaId);
            return AreaForBookmarkResponse.builder()
                    .name(area.getName())
                    .mapPos(MapPos.builder()
                            .x(area.getMapX())
                            .y(area.getMapY())
                            .build())
                    .category(area.getCategory())
                    .address(area.getAddress())
                    .roadAddress(area.getRoadAddress())
                    .link(area.getLink())
                    .telephone(area.getTelePhone())
                    .build();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AREA_READ_FAILED);
        }
    }
}
