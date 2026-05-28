package com.example.plana.dto.region.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "행정 구역 리스트 응답")
public class RegionZdoListResponse {
    @Schema(description = "행정 구역 리스트")
    private List<ZdoResponse> regions;
}
