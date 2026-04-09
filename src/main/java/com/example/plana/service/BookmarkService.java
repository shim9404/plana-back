package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.dto.area.create.AreaPlaceCreateRequest;
import com.example.plana.dto.bookmark.create.BookmarkCreateRequest;
import com.example.plana.mapper.AreaMapper;
import com.example.plana.mapper.BookmarkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkMapper bookmarkMapper;
    private final AreaMapper areaMapper;

    /**
     * 북마크 등록
     * @param tripId 북마크 귀속 여행 ID
     * @param request BookmarkCreateRequest
     *                / AreaPlaceCreateRequest: AREA DB에 존재하지 않는 근처 장소(PLACE)를 북마크 시도하는 경우 필수 (그 외 null로 요청)
     */
    @Transactional
    public void createBookmark(String tripId, BookmarkCreateRequest request) {
        String areaId = "";
        if (request.getArea() != null) {    // AREA DB에 존재하지 않는 근처 장소(PLACE)를 북마크한 경우
            areaId = createNewPlaceAreaBeforeBookmark(request.getArea());
        } else {
            areaId = request.getAreaId();
        }
        log.info(request);
        log.info(areaId);
        Map<String, Object> bookmarkParams = new HashMap<>();
        bookmarkParams.put("tripId", tripId);
        bookmarkParams.put("areaId", areaId);
        bookmarkParams.put("bookmarkType", request.getBookmarkType());

        try {
            bookmarkMapper.createBookmark(bookmarkParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_BOOKMARK_CREATE_FAILED);
        }
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
     * 북마크 삭제
     * @param bookmarkId 삭제할 북마크 ID
     */
    public void deleteBookmark(String bookmarkId) {
        int result = -1;
        try {
            result = bookmarkMapper.deleteBookmark(bookmarkId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_BOOKMARK_DELETE_FAILED);
        }
        if (result == 0) {
            throw new BusinessException(ErrorCode.TRIP_BOOKMARK_NOT_FOUND);
        }
    }
}
