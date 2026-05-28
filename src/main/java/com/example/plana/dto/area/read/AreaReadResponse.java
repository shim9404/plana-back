package com.example.plana.dto.area.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "근처 장소 정보(장소 DB) 응답")
public class AreaReadResponse {
    @Schema(description = "행정구역 id", example = "35020")
    private String regionId;
    @Schema(description = "근처 장소 정보(장소 DB-PLACE)")
    private AreaTypeResponse place;
    @Schema(description = "근처 장소 정보(장소 DB-SPOT)")
    private AreaTypeResponse spot;
    @Schema(description = "근처 장소 정보(장소 DB-FOOD)")
    private AreaTypeResponse food;
}
