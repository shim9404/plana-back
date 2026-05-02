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

    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 초기 로드 - 각 searchType 1페이지씩 반환
     */
    public AreaReadResponse getArea(String regionId) {

        if (regionId != null && regionMapper.checkRegionExists(regionId) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        AreaTypeResponse place = getPagedType(regionId, "PLACE", 1, DEFAULT_PAGE_SIZE);
        AreaTypeResponse spot  = getPagedType(regionId, "SPOT",  1, DEFAULT_PAGE_SIZE);
        AreaTypeResponse food  = getPagedType(regionId, "FOOD",  1, DEFAULT_PAGE_SIZE);

        return new AreaReadResponse(regionId, place, spot, food);
    }

    /**
     * 특정 searchType 페이지 조회
     */
    public AreaTypePageResponse getAreaByType(String regionId, String searchType, int page, int size) {

        if (regionId != null && regionMapper.checkRegionExists(regionId) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        AreaTypeResponse typeResponse = getPagedType(regionId, searchType, page, size);
        return new AreaTypePageResponse(regionId, typeResponse);
    }

    /**
     * 공통 페이징 조회
     */
    private AreaTypeResponse getPagedType(String regionId, String searchType, int page, int size) {

        boolean isZdo = regionId != null && regionId.endsWith("000");

        AreaPageRequest request = AreaPageRequest.builder()
                .regionId(regionId)
                .searchType(searchType)
                .page(page)
                .size(size)
                .build();

        int totalCount;
        List<Area> areas;

        if (isZdo) {
            totalCount = areaMapper.countAreaByZdoCode(request);
            areas = totalCount > 0 ? areaMapper.readAreaByZdoCode(request) : List.of();
        } else {
            totalCount = areaMapper.countAreaByType(request);
            areas = totalCount > 0 ? areaMapper.readAreaByPage(request) : List.of();
        }

        int totalPages = (int) Math.ceil((double) totalCount / size);

        List<AreaDetailResponse> details = areas.stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        return new AreaTypeResponse(searchType, totalCount, totalPages, page, size, details);
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
    public PlaceReadPageResponse readPlace(String keyword, double mapX, double mapY, int page, int size) {
        RestTemplate restTemplate = new RestTemplate();

        log.info("mapX:: "+mapX);
        log.info("mapY:: "+mapY);


        // 카카오 API는 page 파라미터 지원 (1~45), size 파라미터 지원 (1~15 기본값 15)
        // size 최대 15 제한이 있으므로 초과 시 15로 고정
        int kakaoSize = Math.min(size, 15);

        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?query="
                + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=20000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + kakaoSize;

        String urlCategory = "https://dapi.kakao.com/v2/local/search/category.json?category_group_code=FD6"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=20000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + kakaoSize;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kakaoConfig.getClientId());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = (keyword == null || keyword.isEmpty()) ? urlCategory : urlKeyword;

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result;
        try {
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 페이징 메타 정보 꺼내기
        Map<String, Object> meta = (Map<String, Object>) result.get("meta");
        int totalCount = (int) meta.get("total_count");     // 18822 (표시용)
        int pageableCount = (int) meta.get("pageable_count"); // 45 (실제 페이징 가능 수)
        boolean isEnd = (boolean) meta.get("is_end");

        int totalPages = (int) Math.ceil((double) pageableCount / kakaoSize); // 45 / 15 = 3페이지


        List<Map<String, Object>> documents = (List<Map<String, Object>>) result.get("documents");

        List<PlaceReadResponse> list = new ArrayList<>();
        for (Map<String, Object> doc : documents) {
            PlaceReadResponse placeReadResponse = new PlaceReadResponse();
            placeReadResponse.setPlaceId((String) doc.get("id"));
            String areaId = areaMapper.readAreaIdByPlaceId(placeReadResponse.getPlaceId());
            placeReadResponse.setAreaId(areaId);
            placeReadResponse.setSearchType("PLACE");
            placeReadResponse.setName((String) doc.get("place_name"));
            MapPos mapPos = new MapPos();
            mapPos.setX(Double.parseDouble((String) doc.get("x")));
            mapPos.setY(Double.parseDouble((String) doc.get("y")));
            placeReadResponse.setMapPos(mapPos);
            if (doc.get("category_group_code") == null || doc.get("category_group_code").equals("")) {
                placeReadResponse.setCategory("ETC");
            } else {
                placeReadResponse.setCategory((String) doc.get("category_group_code"));
            }
            placeReadResponse.setAddress((String) doc.get("address_name"));
            placeReadResponse.setRoadAddress((String) doc.get("road_address_name"));
            placeReadResponse.setLink((String) doc.get("place_url"));
            placeReadResponse.setTelePhone((String) doc.get("phone"));
            placeReadResponse.setDescription("카카오개발자센터 장소 검색");
            list.add(placeReadResponse);
        }

        return new PlaceReadPageResponse(pageableCount, totalPages, page, kakaoSize, isEnd, list);
    }

    /**
     * 신규 근처 장소 등록
     * AREA DB에 존재하지 않는 근처 장소(PLACE)를 북마크한 경우 호출
     * @param request AreaPlaceCreateRequest
     * @return 등록 완료된 AREA ID
     */
    private String createNewPlaceAreaBeforeBookmark(AreaPlaceCreateRequest request) {
        Map<String, Object> areaParams = new HashMap<>();
        areaParams.put("regionId", request.getRegionId());
        areaParams.put("placeId", request.getPlaceId());
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


    public String getOrCreatePlaceArea(AreaPlaceCreateRequest areaRequest) {
        // placeId로 먼저 조회
        String existingAreaId = areaMapper.readAreaIdByPlaceId(areaRequest.getPlaceId());
        if (existingAreaId != null) {
            return existingAreaId; // 이미 있으면 기존 areaId 반환
        }
        // 없으면 새로 INSERT
        return createNewPlaceAreaBeforeBookmark(areaRequest);
    }
}
