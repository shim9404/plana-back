package com.example.plana.dto.region.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "지역 요약 정보")
public class RegionReadResponse {
    @Schema(description = "행정 구역 ID", example = "32030")
    private String regionId;
    @Schema(description = "도 이름", example = "강원특별자치도")
    private String zdoName;
    @Schema(description = "시군구 이름", example = "강릉시")
    private String siguName;
    @Schema(description = "경도", example = "128.8761")
    private double mapX;
    @Schema(description = "위도", example = "37.7519")
    private double mapY;
}
