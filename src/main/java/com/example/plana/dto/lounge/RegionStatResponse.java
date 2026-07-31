package com.example.plana.dto.lounge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행의 지역 비율 반환")
public class RegionStatResponse {
    @Schema(description = "시/도 이름", example = "경기도")
    private String region;

    @Schema(description = "비율 (%)", example = "100")
    private Integer ratio;
}
