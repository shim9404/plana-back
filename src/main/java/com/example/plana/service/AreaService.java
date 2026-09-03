package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.config.KakaoConfig;
import com.example.plana.config.VisitKoreaConfig;
import com.example.plana.dto.area.create.AreaPlaceCreateRequest;
import com.example.plana.dto.area.read.*;
import com.example.plana.dto.area.read.place.PlaceReadPageResponse;
import com.example.plana.dto.area.read.place.PlaceReadResponse;
import com.example.plana.dto.area.read.place.api.PlaceApiProcessReadResponse;
import com.example.plana.dto.area.read.theme.*;
import com.example.plana.dto.area.read.theme.api.ThemeApiProcessReadResponse;
import com.example.plana.mapper.AreaMapper;
import com.example.plana.mapper.RegionMapper;
import com.example.plana.model.Area;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@EnableCaching
@SpringBootApplication
public class AreaService {
    private final AreaMapper areaMapper;
    private final KakaoConfig kakaoConfig; // kakao apiKey
    private final VisitKoreaConfig visitKoreaConfig; // visitKorea apiKey
    private final RegionMapper regionMapper;
    private final CacheThemeService cacheThemeService;
    private final CachePlaceService cachePlaceService;

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
        areaDetail.setTelephone(area.getTelephone());
        areaDetail.setDescription(area.getDescription());
        areaDetail.setBookmarkCount(area.getBookmarkCount());
        areaDetail.setCreateDate(area.getCreateDate());
        areaDetail.setLatestDate(area.getLatestDate());
        areaDetail.setStatus(area.getStatus());

        return areaDetail;
    }

    // 근처 장소 검색(API)
    public PlaceReadPageResponse readPlace(List<String> category, String keyword, double mapX, double mapY, int page, int size) {
        // 카카오 API는 page 파라미터 지원 (1~45), size 파라미터 지원 (1~15 기본값 15)
        // size 최대 10 제한이 있으므로 초과 시 10로 고정
        int dataSize = Math.min(size, 10);

        // CT1,     FD6,   AT4,    CE7,  AD5
        // 문화시설, 음식점, 관광명소, 카페, 숙박
        // Cache에서 가져오기( 결과 리스트 )
        PlaceApiProcessReadResponse ctResult = null;
        PlaceApiProcessReadResponse fdResult = null;
        PlaceApiProcessReadResponse atResult = null;
        PlaceApiProcessReadResponse ceResult = null;
        PlaceApiProcessReadResponse adResult = null;

        // Cache에서 가져오기 ( 총 개수 )
        int ctCount = 0;
        int fdCount = 0;
        int atCount = 0;
        int ceCount = 0;
        int adCount = 0;

        boolean keywordSearch = keyword != null && !keyword.isBlank();
        // API 분기 처리
        // CT1 - 문화시설
        if (category.contains("CT1")) {
            if (!keywordSearch) { // 키워드 X
                ctResult = cachePlaceService.readCTTravelsbyLocation(mapX, mapY, 1, dataSize);
            } else { // 키워드 O
                ctResult = cachePlaceService.readCTTravelsbyKeyword(keyword, mapX, mapY, 1, dataSize);
            }

            ctCount = ctResult.getTotalCount();
        }

        // FD6 - 음식점
        if (category.contains("FD6")) {
            if (!keywordSearch) { // 키워드 X
                fdResult = cachePlaceService.readFDTravelsbyLocation(mapX, mapY, 1, dataSize);
            } else { // 키워드 O
                fdResult = cachePlaceService.readFDTravelsbyKeyword(keyword, mapX, mapY, 1, dataSize);
            }

            fdCount = fdResult.getTotalCount();
        }

        // AT4 - 관광명소
        if (category.contains("AT4")) {
            if (!keywordSearch) { // 키워드 X
                atResult = cachePlaceService.readATTravelsbyLocation(mapX, mapY, 1, dataSize);
            } else { // 키워드 O
                atResult = cachePlaceService.readATTravelsbyKeyword(keyword, mapX, mapY, 1, dataSize);
            }

            atCount = atResult.getTotalCount();
        }

        // CE7 - 카페
        if (category.contains("CE7")) {
            if (!keywordSearch) { // 키워드 X
                ceResult = cachePlaceService.readCETravelsbyLocation(mapX, mapY, 1, dataSize);
            } else { // 키워드 O
                ceResult = cachePlaceService.readCETravelsbyKeyword(keyword, mapX, mapY, 1, dataSize);
            }

            ceCount = ceResult.getTotalCount();
        }

        // AD5 - 숙박
        if (category.contains("AD5")) {
            if (!keywordSearch) { // 키워드 X
                adResult = cachePlaceService.readADTravelsbyLocation(mapX, mapY, 1, dataSize);
            } else { // 키워드 O
                adResult = cachePlaceService.readADTravelsbyKeyword(keyword, mapX, mapY, 1, dataSize);
            }

            adCount = adResult.getTotalCount();
        }

        List<PlaceReadResponse> placeTravels = new ArrayList<>();

        // totalCount 합치기
        int totalCount = ctCount + fdCount + atCount + ceCount + adCount;
        int totalPages = (int)Math.ceil((double)totalCount/dataSize); // 총 페이지 수
        if (totalCount == 0) { return new PlaceReadPageResponse(totalCount, totalPages, page, dataSize, placeTravels); } // 결과 데이터가 0개 일 경우, 빠져나오기

        // 필요한 범위 계산
        int start = (page-1)*dataSize+1;
        int end = start+dataSize-1;
        int remain = dataSize;


        // CT1 범위 삽입
        int ctStart=1;
        int ctEnd=ctCount;

        List<PlaceReadResponse> ctList = new ArrayList<>();

        if(ctCount>0 && start<=ctEnd && end>=ctStart){
            int ctIndex=Math.max(start,ctStart)-ctStart+1;

            while(remain>0 && ctIndex<=ctCount){
                int apiPage=(ctIndex-1)/dataSize+1;

                // 이미 조회한 1페이지 데이터 재사용
                if (apiPage == 1) {
                    ctList = ctResult.getPlaceList();
                } else {
                    if (!keywordSearch) {
                        ctList = cachePlaceService.readCTTravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                    } else {
                        ctList = cachePlaceService.readCTTravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                    }
                }

                int localStart=(ctIndex-1)%dataSize;
                int localEnd=Math.min(ctList.size(), localStart+remain);

                List<PlaceReadResponse> ctSliceList= ctList.subList(localStart,localEnd);
                placeTravels.addAll(ctSliceList);

                remain -= ctSliceList.size();
                ctIndex += ctSliceList.size();

                if(ctSliceList.isEmpty()) break;
            }
        }

        // FD6 범위 삽입
        int fdStart=ctEnd+1;
        int fdEnd=ctEnd+fdCount;

        List<PlaceReadResponse> fdList = new ArrayList<>();

        if(remain>0 && fdCount>0 && start<=fdEnd && end>=fdStart){
            int fdIndex=Math.max(start,fdStart)-fdStart+1;

            while(remain>0 && fdIndex<=fdCount){
                int apiPage=(fdIndex-1)/dataSize+1;

                // 이미 조회한 1페이지 데이터 재사용
                if (apiPage == 1) {
                    fdList = fdResult.getPlaceList();
                } else {
                    if (!keywordSearch) {
                        fdList = cachePlaceService.readFDTravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                    } else {
                        fdList = cachePlaceService.readFDTravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                    }
                }

                int localStart=(fdIndex-1)%dataSize;
                int localEnd=Math.min(fdList.size(), localStart+remain);

                List<PlaceReadResponse> fdSliceList= fdList.subList(localStart,localEnd);
                placeTravels.addAll(fdSliceList);

                remain-=fdSliceList.size();
                fdIndex+=fdSliceList.size();

                if(fdSliceList.isEmpty()) break;
            }
        }

        // AT4 범위 삽입
        int atStart=fdEnd+1;
        int atEnd=fdEnd+atCount;

        List<PlaceReadResponse> atList = new ArrayList<>();

        if(remain>0 && atCount>0 && start<=atEnd && end>=atStart){
            int atIndex=Math.max(start,atStart)-atStart+1;

            while(remain>0 && atIndex<=atCount){
                int apiPage=(atIndex-1)/dataSize+1;

                // 이미 조회한 1페이지 데이터 재사용
                if (apiPage == 1) {
                    atList = atResult.getPlaceList();
                } else {
                    if (!keywordSearch) {
                        atList = cachePlaceService.readATTravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                    } else {
                        atList = cachePlaceService.readATTravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                    }
                }

                int localStart=(atIndex-1)%dataSize;
                int localEnd=Math.min(atList.size(), localStart+remain);

                List<PlaceReadResponse> atSliceList= atList.subList(localStart,localEnd);
                placeTravels.addAll(atSliceList);

                remain-=atSliceList.size();
                atIndex+=atSliceList.size();

                if(atSliceList.isEmpty()) break;
            }
        }

        // CE7 범위 삽입
        int ceStart=atEnd+1;
        int ceEnd=atEnd+ceCount;

        List<PlaceReadResponse> ceList = new ArrayList<>();

        if(remain>0 && ceCount>0 && start<=ceEnd && end>=ceStart){
            int ceIndex=Math.max(start,ceStart)-ceStart+1;

            while(remain>0 && ceIndex<=ceCount){
                int apiPage=(ceIndex-1)/dataSize+1;

                // 이미 조회한 1페이지 데이터 재사용
                if (apiPage == 1) {
                    ceList = ceResult.getPlaceList();
                } else {
                    if (!keywordSearch) {
                        ceList = cachePlaceService.readCETravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                    } else {
                        ceList = cachePlaceService.readCETravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                    }
                }

                int localStart=(ceIndex-1)%dataSize;
                int localEnd=Math.min(ceList.size(), localStart+remain);

                List<PlaceReadResponse> ceSliceList= ceList.subList(localStart,localEnd);
                placeTravels.addAll(ceSliceList);

                remain-=ceSliceList.size();
                ceIndex+=ceSliceList.size();

                if(ceSliceList.isEmpty()) break;
            }
        }

        // AD5 범위 삽입
        int adStart=ceEnd+1;
        int adEnd=ceEnd+adCount;

        List<PlaceReadResponse> adList = new ArrayList<>();

        if(remain>0 && adCount>0 && start<=adEnd && end>=adStart){
            int adIndex=Math.max(start,adStart)-adStart+1;

            while(remain>0 && adIndex<=adCount){
                int apiPage=(adIndex-1)/dataSize+1;

                // 이미 조회한 1페이지 데이터 재사용
                if (apiPage == 1) {
                    adList = adResult.getPlaceList();
                } else {
                    if (!keywordSearch) {
                        adList = cachePlaceService.readADTravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                    } else {
                        adList = cachePlaceService.readADTravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                    }
                }

                int localStart=(adIndex-1)%dataSize;
                int localEnd=Math.min(adList.size(), localStart+remain);

                List<PlaceReadResponse> adSliceList= adList.subList(localStart,localEnd);
                placeTravels.addAll(adSliceList);

                remain-=adSliceList.size();
                adIndex+=adSliceList.size();

                if(adSliceList.isEmpty()) break;
            }
        }

        return new PlaceReadPageResponse(totalCount, totalPages, page, dataSize, placeTravels);
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
                    .telephone(area.getTelephone())
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

        // Cache에서 가져오기( 결과 리스트 )
        ThemeApiProcessReadResponse petResult = null;
        ThemeApiProcessReadResponse bfResult = null;
        // PET, BF는 서로 독립적인 외부 API 호출이므로 병렬 처리 (CompletableFuture)
        CompletableFuture<ThemeApiProcessReadResponse> petFuture = null;
        CompletableFuture<ThemeApiProcessReadResponse> bfFuture = null;

        // Cache에서 가져오기 (총 개수 )
        int petCount = 0;
        int bfCount = 0;

        boolean keywordSearch = keyword != null && !keyword.isBlank();
        // API 분기 처리
        // PET 호출
        if (theme.contains("PET")) {
            petFuture = CompletableFuture.supplyAsync(() -> {
                if (!keywordSearch) {
                    return cacheThemeService.readPetTravelsbyLocation( mapX, mapY, 1, dataSize );
                } else {
                    return cacheThemeService.readPetTravelsbyKeyword( keyword, regionId, 1, dataSize );
                }
            });
        }
        // BF 호출
        if (theme.contains("BF")) {
            bfFuture = CompletableFuture.supplyAsync(() -> {
                if (!keywordSearch) {
                    return cacheThemeService.readBFTravelsbyLocation( mapX, mapY, 1, dataSize );
                } else {
                    return cacheThemeService.readBFTravelsbyKeyword( keyword, regionId, 1, dataSize );
                }
            });
        }

        // PET, BF 호출이 모두 끝날 때까지 대기
        if (petFuture != null || bfFuture != null) {
            CompletableFuture.allOf(
                    petFuture != null ? petFuture : CompletableFuture.completedFuture(null),
                    bfFuture != null ? bfFuture : CompletableFuture.completedFuture(null) ).join();
        }

        // 병렬 호출 결과 가져오기
        if (petFuture != null) { petResult = petFuture.join(); petCount = petResult.getTotalCount(); }
        if (bfFuture != null) { bfResult = bfFuture.join(); bfCount = bfResult.getTotalCount(); }

        List<ThemeReadResponse> themeTravels = new ArrayList<>();

        // totalCount 합치기
        int totalCount = petCount + bfCount;
        int totalPages = (int)Math.ceil((double)totalCount/dataSize); // 총 페이지 수
        if (totalCount == 0) { // 결과 데이터가 0개 일 경우, 빠져나오기
            return new ThemeReadPageResponse(totalCount, totalPages, page, dataSize, themeTravels);
        }

        // 필요한 범위 계산
        int start = (page-1)*dataSize+1;
        int end = start+dataSize-1;
        int remain = dataSize;

        // PET 범위 삽입
        int petStart=1;
        int petEnd=petCount;

        List<ThemeReadResponse> petList;
        if(petCount>0 && start<=petEnd && end>=petStart){
            int petIndex=Math.max(start,petStart)-petStart+1;

            while(remain>0 && petIndex<=petCount){
                int apiPage=(petIndex-1)/dataSize+1;

                // 1페이지는 처음 조회한 결과 재사용
                if (apiPage == 1) {
                    petList = petResult.getThemeList();
                } else {
                    if (!keywordSearch) {
                        petList = cacheThemeService.readPetTravelsbyLocation(mapX, mapY, apiPage, dataSize).getThemeList();
                    } else {
                        petList = cacheThemeService.readPetTravelsbyKeyword(keyword, regionId, apiPage, dataSize).getThemeList();
                    }
                }

                int localStart=(petIndex-1)%dataSize;
                int localEnd = Math.min(petList.size(), localStart+remain);

                List<ThemeReadResponse> petSliceList = petList.subList(localStart, localEnd);
                themeTravels.addAll(petSliceList);

                remain -= petSliceList.size();
                petIndex+=petSliceList.size();

                if(petSliceList.isEmpty()) break;
            }
        }

        // BF 범위 삽입
        int bfStart=1+petCount;
        int bfEnd=petCount+bfCount;

        List<ThemeReadResponse> bfList;
        if(remain>0 && bfCount>0 && start<=bfEnd && end>=bfStart){
            int bfIndex=Math.max(start,bfStart)-bfStart+1;


            while(remain>0 && bfIndex<=bfCount){
                int apiPage=(bfIndex-1)/dataSize+1;

                // 1페이지는 처음 조회한 결과 재사용
                if (apiPage == 1) {
                    bfList = bfResult.getThemeList();
                } else {
                    if (!keywordSearch) {
                        bfList = cacheThemeService.readBFTravelsbyLocation(mapX, mapY, apiPage, dataSize).getThemeList();
                    } else {
                        bfList = cacheThemeService.readBFTravelsbyKeyword(keyword, regionId, apiPage, dataSize).getThemeList();
                    }
                }

                int localStart=(bfIndex-1)%dataSize;
                int localEnd = Math.min(bfList.size(), localStart+remain);

                List<ThemeReadResponse> bfSliceList = bfList.subList(localStart, localEnd);
                themeTravels.addAll(bfSliceList);

                remain-=bfSliceList.size();
                bfIndex+=bfSliceList.size();

                if(bfSliceList.isEmpty()) break;
            }

        }

        // 중복 제거
        Map<String,ThemeReadResponse> uniqueMap = new LinkedHashMap<>(); // MAP { KEY : VALUE}
        for(ThemeReadResponse travel : themeTravels){
            uniqueMap.putIfAbsent(
                    travel.getPlaceId(), // Id가 처음 나오면 저장 + 이미 있으면 무시
                    travel
            );
        }
        themeTravels = new ArrayList<>(uniqueMap.values());

        return new ThemeReadPageResponse(totalCount, totalPages, page, dataSize, themeTravels);
    }

    // 여행지 필터 검색(관광포털 API)
    public ThemeReadPageResponse readAround(String filter, double mapX, double mapY, int page, int size) {
        // 관광포털 API는 size 파라미터 지원 (1~10 기본값 10)
        // size 최대 10 제한이 있으므로 초과 시 10로 고정
        int dataSize = Math.min(size, 10);

        List<ThemeReadResponse> aroundTravels = new ArrayList<>();

        // 캠프
        if (filter.equals("CAMP")) { aroundTravels = cacheThemeService.readCampTravelsbyLocation(mapX, mapY, page, dataSize).getThemeList(); }
        // 웰니스
        else if (filter.equals("WELLNESS")) { aroundTravels = cacheThemeService.readWellnessTravelsbyLocation(mapX, mapY, page, dataSize).getThemeList(); }

        // 페이징 메타 정보
        int totalCount = aroundTravels.size();
        int totalPages = (int) Math.ceil((double) aroundTravels.size() / dataSize);

        return new ThemeReadPageResponse(totalCount, totalPages, page, dataSize, aroundTravels);
    }

    // 여행지 연관 검색(관광포털 API)
    public RelatePlaceReadPageResponse readRelatedTravels(String keyword, String regionId, int page, int size) {
        // 관광포털 API는 size 파라미터 지원 (1~10 기본값 10)
        // size 최대 10 제한이 있으므로 초과 시 10로 고정
        int dataSize = Math.min(size, 10);

        List<RelatePlaceReadResponse> relatedTravels = new ArrayList<>();

        relatedTravels = cacheThemeService.readRelatedTravelsbyKeyword(keyword, regionId, page, dataSize).getRelatedList();

        // 페이징 메타 정보
        int totalCount = relatedTravels.size();
        int totalPages = (int) Math.ceil((double) relatedTravels.size() / dataSize);

        return new RelatePlaceReadPageResponse(totalCount, totalPages, page, dataSize, relatedTravels);
    }
}


