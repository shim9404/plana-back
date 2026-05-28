package com.example.plana.dto.area.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "특정 searchType의 페이지 조회 응답")
public class AreaTypePageResponse {
    @Schema(description = "행정구역 id", example = "35020")
    private String regionId;
    @Schema(description = "근처 장소 정보(장소 DB-PLACE/SPOT/FOOD)")
    private AreaTypeResponse data;
}
