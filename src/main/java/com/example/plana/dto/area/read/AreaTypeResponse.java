package com.example.plana.dto.area.read;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "근처 장소 정보(장소 DB-PLACE/SPOT/FOOD)")
public class AreaTypeResponse {
    @Schema(description = "분류", example = "FOOD")
    private String searchType;
    @Schema(description = "총 검색 결과 개수", example = "30")
    private int totalCount;
    @Schema(description = "전체 페이지", example = "2")
    private int totalPages;
    @Schema(description = "현재 보고 있는 페이지", example = "1")
    private int currentPage;
    @Schema(description = "한 페이지에 들어갈 장소 수", example = "20")
    private int pageSize;
    @Schema(description = "근처 장소 정보(장소 DB) 리스트")
    private List<AreaDetailResponse> areas;
}


