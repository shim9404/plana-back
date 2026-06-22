package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.config.VisitKoreaConfig;
import com.example.plana.dto.area.read.MapPos;
import com.example.plana.dto.area.read.ThemeReadResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class CacheThemeService {
    private final VisitKoreaConfig visitKoreaConfig; // visitKorea apiKey

    // 여행지 전체 조회 및 저장(캐싱)
    @Cacheable(
            value = "themeTravels",
            key = "#theme.toString() + '-' + #keyword + '-' + #mapX + '-' + #mapY + '-' + #regionId"
    ) // 위치 좌표, 키워드, 행정구역 ID 변경 시, 새 API 호출
    public List<ThemeReadResponse> readThemeTravels(List<String> theme, String keyword, double mapX, double mapY, String regionId, int dataSize) {
        List<ThemeReadResponse> themeTravels = new ArrayList<>();

        boolean keywordSearch = keyword != null && !keyword.isBlank();
        // API 분기 처리
        if (!keywordSearch) { // 위치 기반 api
            // 반려동물
            if (theme.contains("PET")) { themeTravels.addAll(readPetTravelsbyLocation(mapX, mapY, 1, dataSize));}
            // 무장애
            if (theme.contains("BF")) { themeTravels.addAll(readBFTravelsbyLocation(mapX, mapY, 1, dataSize));}
            // 고캠핑
            // if (theme.contains("CAMP")) { themeTravels.addAll(readCampTravelsbyLocation(mapX, mapY, 1, dataSize));}
        }
        else{ // 키워드 기반 api
            // 반려동물
            if (theme.contains("PET")) { themeTravels.addAll(readPetTravelsbyKeyword(keyword, regionId, 1, dataSize));}
            // 무장애
            if (theme.contains("BF")) { themeTravels.addAll(readBFTravelsbyKeyword(keyword, regionId, 1, dataSize));}
            // 고캠핑
            // if (theme.contains("CAMP")) { themeTravels.addAll(readCampTravelsbyKeyword(keyword, mapX, mapY, 1, dataSize));}
        }

        return themeTravels;
    }


    // 반려동물 관련 여행지 - 지역 기반
    private List<ThemeReadResponse> readPetTravelsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://apis.data.go.kr/B551011/KorPetTourService2/locationBasedList2"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&mapX=" + mapX
                + "&mapY=" + mapY
                + "&radius=20000"
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readAllItems(urlLocation, page, dataSize);

        // 반려동물(PET) api 응답 결과 저장
        List<ThemeReadResponse> list = readPetLists(itemList);

        return list;
    }

    // 반려동물 관련 여행지 - 키워드 기반
    private List<ThemeReadResponse> readPetTravelsbyKeyword(String keyword, String regionId, int page, int dataSize) {
        String urlKeyword = "https://apis.data.go.kr/B551011/KorPetTourService2/searchKeyword2"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&lDongRegnCd=" + regionId.substring(0, 2)
                + "&lDongSignguCd=" + regionId.substring(2)
                + "&keyword=" + keyword
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readAllItems(urlKeyword, page, dataSize);

        // 반려동물(PET) api 응답 결과 저장
        List<ThemeReadResponse> list = readPetLists(itemList);

        return list;
    }

    // 무장애 관련 여행지 - 지역 기반
    private List<ThemeReadResponse> readBFTravelsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://apis.data.go.kr/B551011/KorWithService2/locationBasedList2"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&mapX=" + mapX
                + "&mapY=" + mapY
                + "&radius=20000"
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readAllItems(urlLocation, page, dataSize);

        // 무장애(BF) api 응답 결과 저장
        List<ThemeReadResponse> list = readBFLists(itemList);

        return list;
    }

    // 무장애 관련 여행지 - 키워드 기반
    private List<ThemeReadResponse> readBFTravelsbyKeyword(String keyword, String regionId, int page, int dataSize) {
        String urlKeyword = "https://apis.data.go.kr/B551011/KorWithService2/searchKeyword2"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&lDongRegnCd=" + regionId.substring(0, 2)
                + "&lDongSignguCd=" + regionId.substring(2)
                + "&keyword=" + keyword
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readAllItems(urlKeyword, page, dataSize);

        // 무장애(BF) api 응답 결과 저장
        List<ThemeReadResponse> list = readBFLists(itemList);

        return list;
    }

    // 고캠핑 관련 여행지 - 지역 기반
    private List<ThemeReadResponse> readCampTravelsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://apis.data.go.kr/B551011/GoCamping/locationBasedList"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&mapX=" + mapX
                + "&mapY=" + mapY
                + "&radius=20000"
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readAllItems(urlLocation, page, dataSize);

        // 고캠핑(CAMP) api 응답 결과 저장
        List<ThemeReadResponse> list = readCampLists(itemList, mapX, mapY);

        return list;
    }

    // 고캠핑 관련 여행지 - 키워드 기반
    private List<ThemeReadResponse> readCampTravelsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://apis.data.go.kr/B551011/GoCamping/searchList"
                + "?serviceKey=" + visitKoreaConfig.getServiceKey()
                + "&MobileOS=WEB" + "&MobileApp=PLANA" + "&_type=json"
                + "&keyword=" + keyword
                + "&pageNo=" + page
                + "&numOfRows=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readAllItems(urlKeyword, page, dataSize);

        // 고캠핑(CAMP) api 응답 결과 저장
        List<ThemeReadResponse> list = readCampLists(itemList, mapX, mapY);

        return list;
    }

    // api 결과 데이터 모두 조회
    private List<Map<String, Object>> readAllItems(String url, int page, int dataSize) {

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        List<Map<String, Object>> allItems = new ArrayList<>();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Map<String, Object> result;
        try {
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> responseMap = (Map<String, Object>) result.get("response");
        Map<String, Object> body = (Map<String, Object>) responseMap.get("body");

        // 데이터 총 개수 조회
        int totalCount = Integer.parseInt(body.get("totalCount").toString());

        if (totalCount == 0) { return allItems; } // 결과 데이터가 0개 일 경우, 빠져나오기
        // 총 결과 데이터 수 통해 총 페이지 수량 조회
        int totalPages = (int)Math.ceil((double) totalCount / dataSize);

        // 모든 페이지 조회
        for (int currentPage = 1; currentPage <= totalPages; currentPage++) {
            String currentUrl = url.replace("pageNo=" + page, "pageNo=" + currentPage);

            ResponseEntity<String> pageResponse = restTemplate.getForEntity(currentUrl, String.class);
            Map<String, Object> pageResult;
            try {
                pageResult = objectMapper.readValue(response.getBody(), Map.class);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            Map<String, Object> pageResponseMap = (Map<String, Object>) pageResult.get("response");
            Map<String, Object> pageBody = (Map<String, Object>) pageResponseMap.get("body");
            Map<String, Object> items = (Map<String, Object>) pageBody.get("items");

            List<Map<String,Object>> itemList = (List<Map<String,Object>>) items.get("item");

            allItems.addAll(itemList);
        }

        return allItems;
    }

    // 반려동물(PET) api 응답 결과 저장
    private List<ThemeReadResponse> readPetLists(List<Map<String,Object>> itemList){
        List<ThemeReadResponse> list = new ArrayList<>();
        for (Map<String, Object> item : itemList) {
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
            String searchTheme = sortContent(contentTypeId);
            themeReadResponse.setCategory(searchTheme);
            // 도로명 주소 (지번 주소 X)
            themeReadResponse.setRoadAddress((String) item.get("addr1"));
            // 링크 (X)
            // 전화번호 (X)
            // 설명
            themeReadResponse.setDescription("관광포털 여행지 검색(PET)");

            list.add(themeReadResponse);
        }

        return list;
    }

    // 무장애(BF) api 응답 결과 저장
    private List<ThemeReadResponse> readBFLists(List<Map<String,Object>> itemList){
        List<ThemeReadResponse> list = new ArrayList<>();
        for (Map<String, Object> item : itemList) {
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
            String searchTheme = sortContent(contentTypeId);
            themeReadResponse.setCategory(searchTheme);
            // 도로명 주소 (지번 주소 X)
            themeReadResponse.setRoadAddress((String) item.get("addr1"));
            // 링크 (X)
            // 전화번호 (X)
            // 설명
            themeReadResponse.setDescription("관광포털 여행지 검색(BF)");

            list.add(themeReadResponse);
        }

        return list;
    }

    // 콘텐츠 타입 ID -> 장소 분류 코드로 변경
    private String sortContent(int contentTypeId){
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
        return searchTheme;
    }

    // 고캠핑(CAMP) api 응답 결과 저장
    private List<ThemeReadResponse> readCampLists(List<Map<String,Object>> itemList, double mapX, double mapY){
        List<ThemeReadResponse> list = new ArrayList<>();
        for (Map<String, Object> item : itemList) {
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

            list.add(themeReadResponse);
        }

        return list;
    }

}
