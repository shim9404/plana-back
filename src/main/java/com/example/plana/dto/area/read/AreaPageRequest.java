package com.example.plana.dto.area.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "특정 searchType의 페이지 조회 요청")
public class AreaPageRequest {
    @Schema(description = "행정구역 id", example = "35020")
    private String regionId;
    @Schema(description = "분류", example = "FOOD")
    private String searchType;
    @Schema(description = "불러올 페이지 번호", example = "1")
    private int page;
    @Schema(description = "한 페이지에 들어갈 장소 수", example = "20")
    private int size;
    @Schema(description = "키워드", example = "null", nullable = true)
    private String keyword;

    public int getOffset() {
        return (page - 1) * size;
    }

    public String getZdoCode() {
        return regionId != null ? regionId.substring(0, 2) : null;
    }
}
