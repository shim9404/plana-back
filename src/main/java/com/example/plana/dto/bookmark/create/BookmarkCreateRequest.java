package com.example.plana.dto.bookmark.create;

import com.example.plana.dto.area.create.AreaPlaceCreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "북마크 생성 요청")
public class BookmarkCreateRequest {
    @Schema(description = "북마크 타입(등록 색상)", example = "RED")
    private String bookmarkType;
    @Schema(description = "북마크할 장소 ID", example = "null", nullable = true)
    private String areaId;
    private AreaPlaceCreateRequest area;
}
