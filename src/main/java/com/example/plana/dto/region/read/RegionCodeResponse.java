package com.example.plana.dto.region.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "지역 코드 응답")
public class RegionCodeResponse {
    @Schema(description = "행정 구역 ID", example = "32030")
    private String regionId;

    @Schema(description = "도 코드", example = "32")
    private Integer zdoCode;

    @Schema(description = "시군구 코드", example = "30")
    private Integer siguCode;
}