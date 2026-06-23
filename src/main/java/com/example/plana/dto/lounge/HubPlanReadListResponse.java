package com.example.plana.dto.lounge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "허브 목록 응답")
public class HubPlanReadListResponse {
    @Schema(description = "허브 목록")
    private List<HubPlanReadResponse> plans;

    @Schema(description = "전체 항목 수", example = "100")
    private int totalCount;

    @Schema(description = "전체 페이지 수", example = "10")
    private int totalPages;

    @Schema(description = "현재 페이지", example = "1")
    private int currentPage;

    @Schema(description = "페이지당 항목 수", example = "10")
    private int size;

}