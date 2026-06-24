package com.example.plana.dto.lounge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "허브 통계 응답")
public class HubPlanStatResponse {
    @Schema(description = "여행 ID", example = "T1")
    private String tripId;

    // MyBatis에서 raw JSON string으로 받은 뒤 Service에서 파싱 (API 응답 제외)
    @JsonIgnore
    private String categoryStats;

    @JsonIgnore
    private String regionStats;

    @Schema(description = "카테고리 비율 목록")
    private List<CategoryStatResponse> categoryStatList;

    @Schema(description = "지역 비율 목록")
    private List<RegionStatResponse> regionStatList;
}