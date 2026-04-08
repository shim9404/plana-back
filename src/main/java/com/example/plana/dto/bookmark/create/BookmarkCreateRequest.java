package com.example.plana.dto.bookmark.create;

import com.example.plana.dto.area.create.AreaPlaceCreateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkCreateRequest {
    private String bookmarkType;
    private String areaId;
    private AreaPlaceCreateRequest area;
}
