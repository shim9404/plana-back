package com.example.plana.dto.bookmark.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "북마크 복사 요청")
public class BookmarkCopyRequest {
    @Schema(description = "북마크 ID", example = "BM123")
    private String bookmarkId;
    @Schema(description = "북마크 타입(등록 색상)", example = "RED")
    private String bookmarkType;
    @Schema(description = "북마크할 장소 ID", example = "null", nullable = true)
    private String areaId;
}
