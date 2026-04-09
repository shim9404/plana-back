package com.example.plana.dto.bookmark.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkCreateResponse {
    private String bookmarkId;
    private String areaId;
}
