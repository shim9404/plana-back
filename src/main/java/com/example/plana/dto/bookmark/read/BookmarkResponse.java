package com.example.plana.dto.bookmark.read;

import com.example.plana.dto.area.read.AreaForBookmarkResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "북마크 응답")
public class BookmarkResponse {
    @Schema(description = "북마크 ID", example = "BM123")
    private String bookmarkId;
    @Schema(description = "북마크 타입(등록 색상)", example = "RED")
    private String bookmarkType;
    @Schema(description = "등록된 장소 ID", example = "A123")
    private String areaId;
    @Schema(description = "등록된 근처장소 ID", nullable = true, example = "null")
    private String placeId;
    // AREA ID로 가져와야하는 정보를 가공하여 담을 변수 모음
    private AreaForBookmarkResponse areaInfo;
}
