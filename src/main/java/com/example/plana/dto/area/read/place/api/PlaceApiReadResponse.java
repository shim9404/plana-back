package com.example.plana.dto.area.read.place.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "근처 장소 정보(카카오 API) 응답 원본 결과")
public class PlaceApiReadResponse {
    @Schema(description = "총 검색 결과 개수", example = "45")
    private int totalCount;
    @Schema(description = "근처 장소 정보(카카오 API) 결과 리스트")
    private List<Map<String, Object>> items;
}
