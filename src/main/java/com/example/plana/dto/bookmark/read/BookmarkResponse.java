package com.example.plana.dto.bookmark.read;

import com.example.plana.dto.area.read.AreaForBookmarkResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponse {
    private String bookmarkId;
    private String bookmarkType;
    private String areaId;
    private String placeId;
    // AREA ID로 가져와야하는 정보를 가공하여 담을 변수 모음
    private AreaForBookmarkResponse areaInfo;
}
