package com.example.plana.dto.lounge;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Schema(description = "라운지 공개용 내 여행 목록 단건 응답")
public class MyTripForLoungeResponse {
    @Schema(description = "여행 ID", example = "T1")
    private String tripId;

    @Schema(description = "여행 제목", example = "제주도 3박4일")
    private String name;

    @Schema(description = "여행 시작일", example = "2024-01-01")
    private String startDate;

    @Schema(description = "여행 종료일", example = "2024-01-04")
    private String endDate;

    @Schema(description = "여행 박 수", example = "3")
    private Integer nights;

    @Schema(description = "현재 공개 여부", example = "true")
    private Boolean isPublic;

    @Schema(description = "카테고리 비율 목록")
    private List<CategoryStatResponse> categoryStatList;

    @Schema(description = "지역 비율 목록")
    private List<RegionStatResponse> regionStatList;

    @JsonIgnore
    @Schema(hidden = true)
    private String categoryStats;

    @JsonIgnore
    @Schema(hidden = true)
    private String regionStats;
}
