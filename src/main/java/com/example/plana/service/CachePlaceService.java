package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.config.KakaoConfig;
import com.example.plana.dto.area.read.MapPos;
import com.example.plana.dto.area.read.PlaceReadResponse;
import com.example.plana.dto.area.read.ThemeReadResponse;
import com.example.plana.mapper.AreaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class CachePlaceService {
    private final AreaMapper areaMapper;
    private final KakaoConfig kakaoConfig; // kakao apiKey

    // ===== 수량 조회 =====
    // CT1,     FD6,   AT4,    CE7,  AD5
    // 문화시설, 음식점, 관광명소, 카페, 숙박

    // 문화시설(CT1) 관련 여행지 - 지역 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "ctTotalCount",
            key = "#mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readCTTotalsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "CT1"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int ctTotalCount = readCount(urlLocation);

        return ctTotalCount;
    }

    // 문화시설(CT1) 관련 여행지 - 키워드 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "ctTotalCount",
            key = "#keyword + '-' + #mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readCTTotalsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "CT1"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int ctTotalCount = readCount(urlKeyword);

        return ctTotalCount;
    }

    // ----

    // 음식점(FD6) 관련 여행지 - 지역 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "fdTotalCount",
            key = "#mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readFDTotalsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "FD6"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int fdTotalCount = readCount(urlLocation);

        return fdTotalCount;
    }

    // 음식점(FD6) 관련 여행지 - 키워드 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "fdTotalCount",
            key = "#keyword + '-' + #mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readFDTotalsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "FD6"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int fdTotalCount = readCount(urlKeyword);

        return fdTotalCount;
    }

    // ----

    // 관광명소(AT4) 관련 여행지 - 지역 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "atTotalCount",
            key = "#mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readATTotalsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "AT4"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int atTotalCount = readCount(urlLocation);

        return atTotalCount;
    }

    // 관광명소(AT4) 관련 여행지 - 키워드 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "atTotalCount",
            key = "#keyword + '-' + #mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readATTotalsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "AT4"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int atTotalCount = readCount(urlKeyword);

        return atTotalCount;
    }

    // ----

    // 카페(CE7) 관련 여행지 - 지역 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "ceTotalCount",
            key = "#mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readCETotalsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "CE7"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int ceTotalCount = readCount(urlLocation);

        return ceTotalCount;
    }

    // 카페(CE7) 관련 여행지 - 키워드 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "ceTotalCount",
            key = "#keyword + '-' + #mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readCETotalsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "CE7"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int ceTotalCount = readCount(urlKeyword);

        return ceTotalCount;
    }

    // ----

    // 숙박(AD5) 관련 여행지 - 지역 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "adTotalCount",
            key = "#mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readADTotalsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "AD5"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int adTotalCount = readCount(urlLocation);

        return adTotalCount;
    }

    // 숙박(AD5) 관련 여행지 - 키워드 기반 조회 및 저장(캐싱)
    @Cacheable(
            value = "adTotalCount",
            key = "#keyword + '-' + #mapX + '-' + #mapY + '-' + #page + '-' + #dataSize"
    ) // 위치 좌표 변경 시, 새 API 호출
    public int readADTotalsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "AD5"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 수량 조회
        int adTotalCount = readCount(urlKeyword);

        return adTotalCount;
    }

    // ===== 데이터 조회 =====

    // 문화시설(CT1) 관련 여행지 - 지역 기반 조회 및 저장List<PlaceReadResponse>
    public List<PlaceReadResponse> readCTTravelsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "CT1"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlLocation);

        // 문화시설(CT1) api 응답 결과 저장
        List<PlaceReadResponse> ctList = readLists(itemList);

        return ctList;
    }

    // 문화시설(CT1) 관련 여행지 - 키워드 기반 조회 및 저장
    public List<PlaceReadResponse> readCTTravelsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "CT1"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlKeyword);

        // 문화시설(CT1) api 응답 결과 저장
        List<PlaceReadResponse> ctList = readLists(itemList);

        return ctList;
    }

    // ----

    // 음식점(FD6) 관련 여행지 - 지역 기반 조회 및 저장
    public List<PlaceReadResponse> readFDTravelsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "FD6"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlLocation);

        // 음식점(FD6) api 응답 결과 저장
        List<PlaceReadResponse> fdList = readLists(itemList);

        return fdList;
    }

    // 음식점(FD6) 관련 여행지 - 키워드 기반 조회 및 저장
    public List<PlaceReadResponse> readFDTravelsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "FD6"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlKeyword);

        // 음식점(FD6) api 응답 결과 저장
        List<PlaceReadResponse> fdList = readLists(itemList);

        return fdList;
    }

    // ----

    // 관광명소(AT4) 관련 여행지 - 지역 기반 조회 및 저장
    public List<PlaceReadResponse> readATTravelsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "AT4"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlLocation);

        // 관광명소(AT4) api 응답 결과 저장
        List<PlaceReadResponse> atList = readLists(itemList);

        return atList;
    }

    // 관광명소(AT4) 관련 여행지 - 키워드 기반 조회 및 저장
    public List<PlaceReadResponse> readATTravelsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "AT4"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlKeyword);

        // 관광명소(AT4) api 응답 결과 저장
        List<PlaceReadResponse> atList = readLists(itemList);

        return atList;
    }

    // ----

    // 카페(CE7) 관련 여행지 - 지역 기반 조회 및 저장
    public List<PlaceReadResponse> readCETravelsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "CE7"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlLocation);

        // 카페(CE7) api 응답 결과 저장
        List<PlaceReadResponse> ceList = readLists(itemList);

        return ceList;
    }

    // 카페(CE7) 관련 여행지 - 키워드 기반 조회 및 저장
    public List<PlaceReadResponse> readCETravelsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "CE7"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlKeyword);

        // 카페(CE7) api 응답 결과 저장
        List<PlaceReadResponse> ceList = readLists(itemList);

        return ceList;
    }

    // ----

    // 숙박(AD5) 관련 여행지 - 지역 기반 조회 및 저장
    public List<PlaceReadResponse> readADTravelsbyLocation(double mapX, double mapY, int page, int dataSize) {
        String urlLocation = "https://dapi.kakao.com/v2/local/search/category.json?"
                + "category_group_code=" + "AD5"
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlLocation);

        // 숙박(AD5) api 응답 결과 저장
        List<PlaceReadResponse> adList = readLists(itemList);

        return adList;
    }

    // 숙박(AD5) 관련 여행지 - 키워드 기반 조회 및 저장
    public List<PlaceReadResponse> readADTravelsbyKeyword(String keyword, double mapX, double mapY, int page, int dataSize) {
        String urlKeyword = "https://dapi.kakao.com/v2/local/search/keyword.json?"
                + "category_group_code=" + "AD5"
                + "&query=" + keyword
                + "&x=" + mapX
                + "&y=" + mapY
                + "&radius=10000"
                + "&sort=distance"
                + "&page=" + page
                + "&size=" + dataSize;

        // api 결과 데이터 모두 조회
        List<Map<String,Object>> itemList = readItems(urlKeyword);

        // 숙박(AD5) api 응답 결과 저장
        List<PlaceReadResponse> adList = readLists(itemList);

        return adList;
    }

    //  ===== 함수 =====

    // api 결과 수량 조회
    private int readCount(String url) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", kakaoConfig.getClientId());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        Map<String, Object> result;
        try {
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> meta = (Map<String, Object>) result.get("meta");
        // 데이터 총 개수 조회
        int totalCount = (int) meta.get("pageable_count");

        return totalCount;
    }

    // api 결과 데이터 조회
    private List<Map<String, Object>> readItems(String url) {
        List<Map<String,Object>> itemList = new ArrayList<>();

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", kakaoConfig.getClientId());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        Map<String, Object> result;
        try {
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> meta = (Map<String, Object>) result.get("meta");
        // 데이터 총 개수 조회
        int totalCount = (int) meta.get("pageable_count");
        if (totalCount == 0) { return itemList; } // 결과 데이터가 0개 일 경우, 빠져나오기

        itemList = (List<Map<String, Object>>) result.get("documents");

        return itemList;
    }

    // api 응답 결과 저장
    private List<PlaceReadResponse> readLists(List<Map<String,Object>> itemList){
        List<PlaceReadResponse> list = new ArrayList<>();
        for (Map<String, Object> item : itemList) {
            PlaceReadResponse placeReadResponse = new PlaceReadResponse();
            // 여행지 콘텐츠 id
            placeReadResponse.setPlaceId((String) item.get("id"));
            // Area id
            String areaId = areaMapper.readAreaIdByPlaceId(placeReadResponse.getPlaceId());
            placeReadResponse.setAreaId(areaId);
            // 분류
            placeReadResponse.setSearchType("PLACE");
            // 이름
            placeReadResponse.setName((String) item.get("place_name"));
            // 위치 정보
            MapPos mapPos = new MapPos();
            mapPos.setX(Double.parseDouble((String) item.get("x")));
            mapPos.setY(Double.parseDouble((String) item.get("y")));
            placeReadResponse.setMapPos(mapPos);
            // 장소 분류 코드
            placeReadResponse.setCategory((String) item.get("category_group_code"));
            // 지번 주소
            placeReadResponse.setAddress((String) item.get("address_name"));
            // 도로명 주소
            placeReadResponse.setRoadAddress((String) item.get("road_address_name"));
            // 링크
            placeReadResponse.setAddress((String) item.get("place_url"));
            // 전화번호
            placeReadResponse.setAddress((String) item.get("phone"));
            // 설명
            placeReadResponse.setDescription("카카오개발자센터 장소 검색");

            list.add(placeReadResponse);
        }

        return list;
    }
}
