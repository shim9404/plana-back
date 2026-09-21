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

        // CT1,     FD6,   AT4,    CE7,  AD5, SW8
        // 문화시설, 음식점, 관광명소, 카페, 숙박, 교통
        // 키워드 x : 문화 시설 -> 음식점 -> 관광명소 -> 카페 -> 숙박 -> 교통
        // 키워드 o : 교통 -> 문화 시설 -> 음식점 -> 관광명소 -> 카페 -> 숙박

        // Cache에서 가져오기( 결과 리스트 )
        PlaceApiProcessReadResponse ctResult = null;
        PlaceApiProcessReadResponse fdResult = null;
        PlaceApiProcessReadResponse atResult = null;
        PlaceApiProcessReadResponse ceResult = null;
        PlaceApiProcessReadResponse adResult = null;
        PlaceApiProcessReadResponse swResult = null;
        // CT1, FD6, AT4, CE7, AD5, SW8는 서로 독립적인 외부 API 호출이므로 병렬 처리 (CompletableFuture)
        CompletableFuture<PlaceApiProcessReadResponse> ctFuture = null;
        CompletableFuture<PlaceApiProcessReadResponse> fdFuture = null;
        CompletableFuture<PlaceApiProcessReadResponse> atFuture = null;
        CompletableFuture<PlaceApiProcessReadResponse> ceFuture = null;
        CompletableFuture<PlaceApiProcessReadResponse> adFuture = null;
        CompletableFuture<PlaceApiProcessReadResponse> swFuture = null;

        // Cache에서 가져오기 ( 총 개수 )
        int ctCount = 0;
        int fdCount = 0;
        int atCount = 0;
        int ceCount = 0;
        int adCount = 0;
        int swCount = 0;

        boolean keywordSearch = keyword != null && !keyword.isBlank();
        // API 분기 처리
        // CT1 - 문화시설
        if (category.contains("CT1")) {
            ctFuture = CompletableFuture.supplyAsync(() -> {
                if (!keywordSearch) {
                    return cachePlaceService.readCTTravelsbyLocation( mapX, mapY, 1, dataSize );
                } else {
                    return cachePlaceService.readCTTravelsbyKeyword( keyword, mapX, mapY, 1, dataSize );
                }
            });
        }
        // FD6 - 음식점
        if (category.contains("FD6")) {
            fdFuture = CompletableFuture.supplyAsync(() -> {
                if (!keywordSearch) { return cachePlaceService.readFDTravelsbyLocation( mapX, mapY, 1, dataSize );
                } else {
                    return cachePlaceService.readFDTravelsbyKeyword( keyword, mapX, mapY, 1, dataSize );
                }
            });
        }
        // AT4 - 관광명소
        if (category.contains("AT4")) {
            atFuture = CompletableFuture.supplyAsync(() -> {
                if (!keywordSearch) {
                    return cachePlaceService.readATTravelsbyLocation( mapX, mapY, 1, dataSize );
                } else {
                    return cachePlaceService.readATTravelsbyKeyword( keyword, mapX, mapY, 1, dataSize );
                }
            });
        }
        // CE7 - 카페
        if (category.contains("CE7")) {
            ceFuture = CompletableFuture.supplyAsync(() -> {
                if (!keywordSearch) {
                    return cachePlaceService.readCETravelsbyLocation( mapX, mapY, 1, dataSize );
                } else {
                    return cachePlaceService.readCETravelsbyKeyword( keyword, mapX, mapY, 1, dataSize );
                }
            });
        }
        // AD5 - 숙박
        if (category.contains("AD5")) {
            adFuture = CompletableFuture.supplyAsync(() -> {
                if (!keywordSearch) {
                    return cachePlaceService.readADTravelsbyLocation( mapX, mapY, 1, dataSize );
                } else {
                    return cachePlaceService.readADTravelsbyKeyword( keyword, mapX, mapY, 1, dataSize );
                }
            });
        }
        // SW8 - 교통
        if (category.contains("SW8")) {
            swFuture = CompletableFuture.supplyAsync(() -> {
                if (!keywordSearch) {
                    return cachePlaceService.readSWTravelsbyLocation( mapX, mapY, 1, dataSize );
                } else {
                    return cachePlaceService.readSWTravelsbyKeyword( keyword, mapX, mapY, 1, dataSize);
                }
            });
        }

        // 선택된 API 호출이 모두 끝날 때까지 대기
        List<CompletableFuture<PlaceApiProcessReadResponse>> futures = new ArrayList<>();
        if (ctFuture != null) futures.add(ctFuture);
        if (fdFuture != null) futures.add(fdFuture);
        if (atFuture != null) futures.add(atFuture);
        if (ceFuture != null) futures.add(ceFuture);
        if (adFuture != null) futures.add(adFuture);
        if (swFuture != null) futures.add(swFuture);
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 병렬 호출 결과 가져오기
        if (ctFuture != null) { ctResult = ctFuture.join(); ctCount = ctResult.getTotalCount(); }
        if (fdFuture != null) { fdResult = fdFuture.join(); fdCount = fdResult.getTotalCount(); }
        if (atFuture != null) { atResult = atFuture.join(); atCount = atResult.getTotalCount(); }
        if (ceFuture != null) { ceResult = ceFuture.join(); ceCount = ceResult.getTotalCount(); }
        if (adFuture != null) { adResult = adFuture.join(); adCount = adResult.getTotalCount(); }
        if (swFuture != null) { swResult = swFuture.join(); swCount = swResult.getTotalCount(); }

        // 카테고리별 결과/개수를 Map으로 관리
        Map<String, PlaceApiProcessReadResponse> resultMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();

        resultMap.put("CT1", ctResult);
        resultMap.put("FD6", fdResult);
        resultMap.put("AT4", atResult);
        resultMap.put("CE7", ceResult);
        resultMap.put("AD5", adResult);
        resultMap.put("SW8", swResult);

        countMap.put("CT1", ctCount);
        countMap.put("FD6", fdCount);
        countMap.put("AT4", atCount);
        countMap.put("CE7", ceCount);
        countMap.put("AD5", adCount);
        countMap.put("SW8", swCount);

        // 키워드 유무에 따라 카테고리 순서 결정
        List<String> categoryOrder;
        if (keywordSearch) {
            // 키워드 검색
            // 교통 → 문화시설 → 음식점 → 관광명소 → 카페 → 숙박
            categoryOrder = List.of("SW8", "CT1", "FD6", "AT4", "CE7", "AD5");
        } else {
            // 일반 검색
            // 문화시설 → 음식점 → 관광명소 → 카페 → 숙박 → 교통
            categoryOrder = List.of("CT1", "FD6", "AT4", "CE7", "AD5", "SW8");
        }

        // 전체 개수 계산
        int totalCount = 0;
        for (String categoryCode : categoryOrder) { // totalCount 합치기
            totalCount += countMap.get(categoryCode); // 총 개수
        }
        int totalPages = (int) Math.ceil((double) totalCount / dataSize); // 총 페이지 수

        List<PlaceReadResponse> placeTravels = new ArrayList<>();

        if (totalCount == 0) { // 결과 데이터가 0개 일 경우, 빠져나오기
            return new PlaceReadPageResponse(totalCount, totalPages, page, dataSize, placeTravels); }

        // 필요한 범위 계산
        int start = (page - 1) * dataSize + 1;
        int end = start + dataSize - 1;
        int remain = dataSize;

        // 앞에서부터 카테고리별 데이터가 쌓이는 위치
        int categoryStart = 1;

        // 카테고리 순서대로 데이터 삽입
        for (String categoryCode : categoryOrder) {

            // 해당 카테고리의 전체 데이터 개수
            int categoryCount = countMap.get(categoryCode);

            // 해당 카테고리가 선택되지 않았거나 데이터가 없으면 건너뜀
            if (categoryCount <= 0 || remain <= 0) {
                categoryStart += categoryCount;
                continue;
            }

            // 해당 카테고리가 전체 결과에서 차지하는 범위
            int categoryEnd = categoryStart + categoryCount - 1;

            // 현재 요청 페이지와 해당 카테고리 범위가 겹치는지 확인
            if (start <= categoryEnd && end >= categoryStart) {

                // 해당 카테고리에서 가져와야 하는 시작 위치
                int categoryIndex = Math.max(start, categoryStart) - categoryStart + 1;

                while (remain > 0 && categoryIndex <= categoryCount) {
                    // 카카오 API 페이지 계산
                    int apiPage = (categoryIndex - 1) / dataSize + 1;
                    List<PlaceReadResponse> categoryList;

                    // 1페이지는 이미 병렬 호출한 결과 재사용
                    if (apiPage == 1) {
                        PlaceApiProcessReadResponse result = resultMap.get(categoryCode);
                        if (result == null) {break;}
                        categoryList = result.getPlaceList();
                    } else {
                        // 2페이지 이상은 필요한 경우에만 추가 호출
                        if (!keywordSearch) {
                            switch (categoryCode) {
                                case "CT1":
                                    categoryList = cachePlaceService.readCTTravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "FD6":
                                    categoryList = cachePlaceService.readFDTravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "AT4":
                                    categoryList = cachePlaceService.readATTravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "CE7":
                                    categoryList = cachePlaceService.readCETravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "AD5":
                                    categoryList = cachePlaceService.readADTravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "SW8":
                                    categoryList = cachePlaceService.readSWTravelsbyLocation(mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                default:
                                    categoryList = new ArrayList<>();
                            }
                        } else {
                            switch (categoryCode) {
                                case "SW8":
                                    categoryList = cachePlaceService.readSWTravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "CT1":
                                    categoryList = cachePlaceService.readCTTravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "FD6":
                                    categoryList = cachePlaceService.readFDTravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "AT4":
                                    categoryList = cachePlaceService.readATTravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "CE7":
                                    categoryList = cachePlaceService.readCETravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                case "AD5":
                                    categoryList = cachePlaceService.readADTravelsbyKeyword(keyword, mapX, mapY, apiPage, dataSize).getPlaceList();
                                    break;
                                default:
                                    categoryList = new ArrayList<>();
                            }
                        }
                    }

                    // API 응답 데이터가 없으면 종료
                    if (categoryList == null || categoryList.isEmpty()) { break; }

                    // 현재 API 페이지에서 가져올 시작 위치
                    int localStart = (categoryIndex - 1) % dataSize;

                    // 혹시 API 응답 데이터보다 시작 위치가 크면 종료
                    if (localStart >= categoryList.size()) { break; }

                    int localEnd = Math.min(categoryList.size(), localStart + remain);
                    List<PlaceReadResponse> sliceList = categoryList.subList(localStart, localEnd);
                    placeTravels.addAll(sliceList);

                    remain -= sliceList.size();
                    categoryIndex += sliceList.size();

                    // 데이터가 더 이상 들어오지 않으면 종료
                    if (sliceList.isEmpty()) { break;}
                }
            }

            // 다음 카테고리의 시작 위치
            categoryStart = categoryEnd + 1;
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


