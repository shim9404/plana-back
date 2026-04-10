package com.example.plana.service;

import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.dto.bookmark.create.BookmarkCreateRequest;
import com.example.plana.dto.bookmark.create.BookmarkCreateResponse;
import com.example.plana.dto.bookmark.read.BookmarkResponse;
import com.example.plana.dto.common.StatusUpdateRequest;
import com.example.plana.mapper.BookmarkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkMapper bookmarkMapper;
    private final AreaService areaService;

    /**
     * 북마크 등록
     * @param tripId 북마크 귀속 여행 ID
     * @param request BookmarkCreateRequest
     *                / AreaPlaceCreateRequest: AREA DB에 존재하지 않는 근처 장소(PLACE)를 북마크 시도하는 경우 필수 (그 외 null로 요청)
     * @return BookmarkCreateResponse
     */
    @Transactional
    public BookmarkCreateResponse createBookmark(String tripId, BookmarkCreateRequest request) {
        String areaId = "";
        if (request.getArea() != null) {    // AREA DB에 존재하지 않는 근처 장소(PLACE)를 북마크한 경우
            areaId = areaService.createNewPlaceAreaBeforeBookmark(request.getArea());
        } else {
            areaId = request.getAreaId();
        }
        log.info(request);
        log.info(areaId);
        Map<String, Object> bookmarkParams = new HashMap<>();
        bookmarkParams.put("tripId", tripId);
        bookmarkParams.put("areaId", areaId);
        bookmarkParams.put("bookmarkType", request.getBookmarkType());
        bookmarkParams.put("bookmarkId", null);     // OUT
        try {
            bookmarkMapper.createBookmark(bookmarkParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_BOOKMARK_CREATE_FAILED);
        }
        String bookmarkId = (String) bookmarkParams.get("bookmarkId");

        return BookmarkCreateResponse.builder()
                .bookmarkId(bookmarkId)
                .areaId(areaId)
                .build();
    }

    /**
     * 여행 귀속 북마크 전체 조회
     * @param tripId 여행 ID
     * @return List<BookmarkResponse>
     */
    public List<BookmarkResponse> readBookmarksByTripId(String tripId) {
        try {
            // TRIP_ID에 해당하는 모든 BOOKMARK 리스트에 담기
            List<BookmarkResponse> bookmarks = bookmarkMapper.readBookmarks(tripId);
            for (BookmarkResponse bookmark : bookmarks) {
                // BOOKMARK와 연결된 장소의 가공 데이터 요청 후 담기
                bookmark.setAreaInfo(areaService.toBookmarkResponse(bookmark.getAreaId()));
            }
            return bookmarks;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_BOOKMARK_READ_FAILED);
        }
    }

    /**
     * 여행 귀속 북마크 전체 상태 갱신
     * @param tripId 여행 ID
     * @param request BookmarkStatusUpdateRequest
     */
    public void updateBookmarksStatus(String tripId, StatusUpdateRequest request) {
        Map<String, Object> statusParams = new HashMap<>();
        statusParams.put("tripId",    tripId);
        statusParams.put("status", request.getStatus());
        try {
            bookmarkMapper.updateBookmarksStatus(statusParams);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_BOOKMARK_UPDATE_FAILED);
        }
    }

    /**
     * 북마크 단건 삭제
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

    /**
     * 여행 귀속 북마크 전체 삭제
     * @param tripId 여행 ID
     */
    public void deleteBookmarksByTripId(String tripId) {
        try {   // 북마크 전체 삭제
            bookmarkMapper.deleteBookmarksByTripId(tripId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TRIP_BOOKMARK_DELETE_FAILED);
        }
    }

}
