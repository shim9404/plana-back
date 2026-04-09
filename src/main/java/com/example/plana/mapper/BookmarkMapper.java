package com.example.plana.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface BookmarkMapper {
    void createBookmark(Map<String, Object> params);
    int deleteBookmark(String bookmarkId);
    void deleteBookmarksByTripId(String tripId);
}
