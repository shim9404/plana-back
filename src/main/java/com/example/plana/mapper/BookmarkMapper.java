package com.example.plana.mapper;

import com.example.plana.dto.bookmark.read.BookmarkResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface BookmarkMapper {
    void createBookmark(Map<String, Object> params);
    List<BookmarkResponse> readBookmarks(String tripId);
    void updateBookmarksStatus(Map<String, Object> params);
    int deleteBookmark(String bookmarkId);
    void deleteBookmarksByTripId(String tripId);
}
