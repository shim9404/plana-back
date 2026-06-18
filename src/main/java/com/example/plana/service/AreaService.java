package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.config.KakaoConfig;
import com.example.plana.config.VisitKoreaConfig;
import com.example.plana.dto.area.create.AreaPlaceCreateRequest;
import com.example.plana.dto.area.read.*;
import com.example.plana.dto.bookmark.read.BookmarkResponse;
import com.example.plana.mapper.AreaMapper;
import com.example.plana.mapper.RegionMapper;
import com.example.plana.model.Area;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final VisitKoreaConfig visitKoreaConfig; // visitKorea apiKey
    private final RegionMapper regionMapper;

    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 초기 로드 - 각 searchType 1페이지씩 반환
     */
    public AreaReadResponse getArea(String regionId) {

        if (regionId != null && regionMapper.checkRegionExists(regionId) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        AreaTypeResponse place = getPagedType(regionId, "PLACE", 1, DEFAULT_PAGE_SIZE, "");
        AreaTypeResponse spot  = getPagedType(regionId, "SPOT",  1, DEFAULT_PAGE_SIZE, "");
        AreaTypeResponse food  = getPagedType(regionId, "FOOD",  1, DEFAULT_PAGE_SIZE, "");

        return new AreaReadResponse(regionId, place, spot, food);
    }

    /**
     * 특정 searchType 페이지 조회
     */
    public AreaTypePageResponse getAreaByType(String regionId, String searchType, int page, int size, String keyword) {

        if (regionId != null && regionMapper.checkRegionExists(regionId) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        AreaTypeResponse typeResponse = getPagedType(regionId, searchType, page, size, keyword);
        return new AreaTypePageResponse(regionId, typeResponse);
    }

    /**
     * 공통 페이징 조회
     */
    private AreaTypeResponse getPagedType(String regionId, String searchType, int page, int size, String keyword) {
        log.info("keyword getPagedType:: "+keyword);
        boolean isZdo = regionId != null && regionId.endsWith("000");

        AreaPageRequest request = AreaPageRequest.builder()
                .regionId(regionId)
                .searchType(searchType)
                .page(page)
                .size(size)
                .keyword(keyword)
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


    // 맞춤 테마의 여행지 검색(관광포털 API)
    public ThemeReadPageResponse readTheme(List<String> theme, String keyword, double mapX, double mapY, String regionId, int page, int size) {
        // 관광포털 API는 size 파라미터 지원 (1~10 기본값 10)
        // size 최대 10 제한이 있으므로 초과 시 10로 고정
        int dataSize = Math.min(size, 10);

        List<ThemeReadResponse> themeTravels = new ArrayList<>();

        if (theme.contains("PET")) { // 반려동물
            themeTravels.addAll(readPetThemeTravels(keyword, mapX, mapY, regionId, 1, dataSize));
        }

        if (theme.contains("BF")) { // 무장애
            themeTravels.addAll(readBFThemeTravels(keyword, mapX, mapY, regionId, 1, dataSize));
        }

        if (theme.contains("CAMP")) { // 고캠핑
            themeTravels.addAll(readCampThemeTravels(keyword, mapX, mapY, regionId, 1, dataSize));
        }

        // 페이징 메타 정보
        int totalCount = themeTravels.size();
        int totalPages = (int) Math.ceil((double) themeTravels.size() / 15);
        // page에 따라 themeTravels 데이터 자르기
        int startIndex = (page - 1) * 15;
        int endIndex = Math.min(startIndex + 15, totalCount);
        List<ThemeReadResponse> pageThemeTravels = themeTravels.subList(startIndex, endIndex);

        return new ThemeReadPageResponse(totalCount, totalPages, page, 15, pageThemeTravels);
    }

    // 반려동물 관련 여행지
    private List<ThemeReadResponse> readPetThemeTravels(String keyword, double mapX, double mapY, String regionId, int page, int dataSize) {
        RestTemplate restTemplate = new RestTemplate();

        String urlLocation = "https://apis.data.go.kr/B551011/KorPetTourService2/locationBasedList2"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&mapX=" + mapX
                + "&mapY=" + mapY
                + "&radius=20000"
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        String urlKeyword = "https://apis.data.go.kr/B551011/KorPetTourService2/searchKeyword2"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&lDongRegnCd=" + regionId.substring(0, 2)
                + "&lDongSignguCd=" + regionId.substring(2)
                + "&keyword=" + keyword
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        String url = (keyword == null || keyword.isEmpty()) ? urlLocation : urlKeyword;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result;
        try {
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> responseMap = (Map<String, Object>) result.get("response");
        Map<String, Object> body = (Map<String, Object>) responseMap.get("body");
        // 결과 데이터가 0개 일 경우, 빠져나오기
        int numOfRows = Integer.parseInt(body.get("numOfRows").toString());
        if (numOfRows == 0) {
            return new ArrayList<>();
        }
        // 모든 결과 데이터 가져오기(모든 페이지)
        int totalCount = Integer.parseInt(body.get("totalCount").toString());
        if (totalCount == 0) {
            return new ArrayList<>();
        }
        int totalPages = (int) Math.ceil((double) totalCount / dataSize);
        List<ThemeReadResponse> list = new ArrayList<>();
        for (int currentPage = 1; currentPage <= totalPages; currentPage++) {
            String currentUrl = url.replace("pageNo=" + page, "pageNo=" + currentPage);

            ResponseEntity<String> pageResponse = restTemplate.getForEntity(currentUrl, String.class);

            Map<String, Object> pageResult;
            try {
                pageResult = objectMapper.readValue(pageResponse.getBody(), Map.class);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            Map<String, Object> pageResponseMap = (Map<String, Object>) pageResult.get("response");
            Map<String, Object> pageBody = (Map<String, Object>) pageResponseMap.get("body");
            Map<String, Object> pageItems = (Map<String, Object>) pageBody.get("items");
            List<Map<String, Object>> pageItemList = (List<Map<String, Object>>) pageItems.get("item");

            for (Map<String, Object> item : pageItemList) {
                ThemeReadResponse themeReadResponse = new ThemeReadResponse();
                // 여행지 콘텐츠 id
                themeReadResponse.setThemeId((String) item.get("contentid"));
                // Area id
                // String areaId = areaMapper.readAreaIdByPlaceId(themeReadResponse.getThemeId());
                // themeReadResponse.setAreaId(areaId);
                // 분류
                themeReadResponse.setSearchType("Theme");
                // 맞춤 테마 종류
                themeReadResponse.setSearchTheme("PET");
                // 이름
                themeReadResponse.setName((String) item.get("title"));
                // 위치 정보
                MapPos mapPos = new MapPos();
                mapPos.setX(Double.parseDouble((String) item.get("mapx")));
                mapPos.setY(Double.parseDouble((String) item.get("mapy")));
                themeReadResponse.setMapPos(mapPos);
                // 장소 분류 코드
                int contentTypeId = Integer.parseInt(item.get("contenttypeid").toString());
                String searchTheme = "";
                switch (contentTypeId) {
                    // 관광지
                    case 12:
                        searchTheme = "AT4";
                        break;
                    case 14: // 문화시설
                        searchTheme = "CT1";
                        break;
                    case 15: // 축제/행사
                        searchTheme = "AT4";
                        break;
                    case 28: // 레포츠
                        searchTheme = "AT4";
                        break;
                    case 32: // 숙박
                        searchTheme = "AD5";
                        break;
                    case 18: // 쇼핑
                        searchTheme = "MT1";
                        break;
                    case 39: // 음식점
                        searchTheme = "FD6";
                        break;
                    default:
                        searchTheme = "ETC";
                }
                themeReadResponse.setCategory(searchTheme);
                // 도로명 주소 (지번 주소 X)
                themeReadResponse.setRoadAddress((String) item.get("addr1"));
                // 링크 (X)
                // 전화번호 (X)
                // 설명
                themeReadResponse.setDescription("관광포털 여행지 검색(PET)");

                list.add(themeReadResponse);
            }

        }

        return list;
    }

    // 무장애 관련 여행지
    private List<ThemeReadResponse> readBFThemeTravels(String keyword, double mapX, double mapY, String regionId, int page, int dataSize) {

        RestTemplate restTemplate = new RestTemplate();

        String urlLocation = "https://apis.data.go.kr/B551011/KorWithService2/locationBasedList2"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&mapX=" + mapX
                + "&mapY=" + mapY
                + "&radius=20000"
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        String urlKeyword = "https://apis.data.go.kr/B551011/KorWithService2/searchKeyword2"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&lDongRegnCd=" + regionId.substring(0, 2)
                + "&lDongSignguCd=" + regionId.substring(2)
                + "&keyword=" + keyword
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        String url = (keyword == null || keyword.isEmpty()) ? urlLocation : urlKeyword;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result;
        try {
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> responseMap = (Map<String, Object>) result.get("response");
        Map<String, Object> body = (Map<String, Object>) responseMap.get("body");
        // 결과 데이터가 0개 일 경우, 빠져나오기
        int numOfRows = Integer.parseInt(body.get("numOfRows").toString());
        if (numOfRows == 0) {
            return new ArrayList<>();
        }
        // 모든 결과 데이터 가져오기(모든 페이지)
        int totalCount = Integer.parseInt(body.get("totalCount").toString());
        if (totalCount == 0) {
            return new ArrayList<>();
        }
        int totalPages = (int) Math.ceil((double) totalCount / dataSize);
        List<ThemeReadResponse> list = new ArrayList<>();
        for (int currentPage = 1; currentPage <= totalPages; currentPage++) {
            String currentUrl = url.replace("pageNo=" + page, "pageNo=" + currentPage);

            ResponseEntity<String> pageResponse = restTemplate.getForEntity(currentUrl, String.class);

            Map<String, Object> pageResult;
            try {
                pageResult = objectMapper.readValue(pageResponse.getBody(), Map.class);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            Map<String, Object> pageResponseMap = (Map<String, Object>) pageResult.get("response");
            Map<String, Object> pageBody = (Map<String, Object>) pageResponseMap.get("body");
            Map<String, Object> pageItems = (Map<String, Object>) pageBody.get("items");
            List<Map<String, Object>> pageItemList = (List<Map<String, Object>>) pageItems.get("item");

            for (Map<String, Object> item : pageItemList) {
                ThemeReadResponse themeReadResponse = new ThemeReadResponse();
                // 여행지 콘텐츠 id
                themeReadResponse.setThemeId((String) item.get("contentid"));
                // Area id
                // String areaId = areaMapper.readAreaIdByPlaceId(themeReadResponse.getThemeId());
                // themeReadResponse.setAreaId(areaId);
                // 분류
                themeReadResponse.setSearchType("Theme");
                // 맞춤 테마 종류
                themeReadResponse.setSearchTheme("BF");
                // 이름
                themeReadResponse.setName((String) item.get("title"));
                // 위치 정보
                MapPos mapPos = new MapPos();
                mapPos.setX(Double.parseDouble((String) item.get("mapx")));
                mapPos.setY(Double.parseDouble((String) item.get("mapy")));
                themeReadResponse.setMapPos(mapPos);
                // 장소 분류 코드
                int contentTypeId = Integer.parseInt(item.get("contenttypeid").toString());
                String searchTheme = "";
                switch (contentTypeId) {
                    // 관광지
                    case 12:
                        searchTheme = "AT4";
                        break;
                    case 14: // 문화시설
                        searchTheme = "CT1";
                        break;
                    case 15: // 축제/행사
                        searchTheme = "AT4";
                        break;
                    case 28: // 레포츠
                        searchTheme = "AT4";
                        break;
                    case 32: // 숙박
                        searchTheme = "AD5";
                        break;
                    case 18: // 쇼핑
                        searchTheme = "MT1";
                        break;
                    case 39: // 음식점
                        searchTheme = "FD6";
                        break;
                    default:
                        searchTheme = "ETC";
                }
                themeReadResponse.setCategory(searchTheme);
                // 도로명 주소 (지번 주소 X)
                themeReadResponse.setRoadAddress((String) item.get("addr1"));
                // 링크 (X)
                // 전화번호 (X)
                // 설명
                themeReadResponse.setDescription("관광포털 여행지 검색(BF)");

                list.add(themeReadResponse);
            }

        }

        return list;
    }

    // 고캠핑 관련 여행지
    private List<ThemeReadResponse> readCampThemeTravels(String keyword, double mapX, double mapY, String regionId, int page, int dataSize) {

        RestTemplate restTemplate = new RestTemplate();

        String urlLocation = "https://apis.data.go.kr/B551011/GoCamping/locationBasedList"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&mapX=" + mapX
                + "&mapY=" + mapY
                + "&radius=20000"
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        String urlKeyword = "https://apis.data.go.kr/B551011/GoCamping/searchList"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&keyword=" + keyword
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        String url = (keyword == null || keyword.isEmpty()) ? urlLocation : urlKeyword;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result;
        try {
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> responseMap = (Map<String, Object>) result.get("response");
        Map<String, Object> body = (Map<String, Object>) responseMap.get("body");
        // 결과 데이터가 0개 일 경우, 빠져나오기
        int numOfRows = Integer.parseInt(body.get("numOfRows").toString());
        if (numOfRows == 0) {
            return new ArrayList<>();
        }
        // 모든 결과 데이터 가져오기(모든 페이지)
        int totalCount = Integer.parseInt(body.get("totalCount").toString());
        if (totalCount == 0) {
            return new ArrayList<>();
        }
        int totalPages = (int) Math.ceil((double) totalCount / dataSize);
        List<ThemeReadResponse> list = new ArrayList<>();
        for (int currentPage = 1; currentPage <= totalPages; currentPage++) {
            String currentUrl = url.replace("pageNo=" + page, "pageNo=" + currentPage);

            ResponseEntity<String> pageResponse = restTemplate.getForEntity(currentUrl, String.class);

            Map<String, Object> pageResult;
            try {
                pageResult = objectMapper.readValue(pageResponse.getBody(), Map.class);
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            Map<String, Object> pageResponseMap = (Map<String, Object>) pageResult.get("response");
            Map<String, Object> pageBody = (Map<String, Object>) pageResponseMap.get("body");
            Map<String, Object> pageItems = (Map<String, Object>) pageBody.get("items");
            List<Map<String, Object>> pageItemList = (List<Map<String, Object>>) pageItems.get("item");

            for (Map<String, Object> item : pageItemList) {
                ThemeReadResponse themeReadResponse = new ThemeReadResponse();
                // 여행지 콘텐츠 id
                themeReadResponse.setThemeId((String) item.get("contentId"));
                // Area id
                // String areaId = areaMapper.readAreaIdByPlaceId(themeReadResponse.getThemeId());
                // themeReadResponse.setAreaId(areaId);
                // 분류
                themeReadResponse.setSearchType("Theme");
                // 맞춤 테마 종류
                themeReadResponse.setSearchTheme("CAMP");
                // 이름
                themeReadResponse.setName((String) item.get("facltNm"));
                // 위치 정보
                MapPos mapPos = new MapPos();
                mapPos.setX(Double.parseDouble((String) item.get("mapX")));
                mapPos.setY(Double.parseDouble((String) item.get("mapY")));
                themeReadResponse.setMapPos(mapPos);
                // 장소 분류 코드
                themeReadResponse.setCategory("AD5"); // 숙박
                // 도로명 주소 (지번 주소 X)
                themeReadResponse.setRoadAddress((String) item.get("addr1"));
                // 링크
                themeReadResponse.setLink((String) item.get("homepage"));
                // 전화번호
                themeReadResponse.setTelePhone((String) item.get("tel"));
                // 설명
                themeReadResponse.setDescription("관광포털 여행지 검색(CAMP)");

                // 거리 계산 및 저장 여부 결정(고캠핑 경우, 키워드 검색 api는 거리 기준이 없음)
                double campX = Double.parseDouble(item.get("mapX").toString());
                double campY = Double.parseDouble(item.get("mapY").toString());

                double distance = calculateDistance(mapY, mapX, campY, campX);

                if (distance <= 20000) { // 지정 거리보다 가까울 경우에만 저장
                    list.add(themeReadResponse);
                }
            }

        }

        return list;
    }

    // Haversine 공식(거리 계산- MAP X, MAP Y, Radius)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // m

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}


