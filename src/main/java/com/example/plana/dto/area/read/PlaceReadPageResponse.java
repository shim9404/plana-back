package com.example.plana.dto.area.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "근처 장소 정보(카카오 API) 응답")
public class PlaceReadPageResponse {
    @Schema(description = "총 검색 결과 개수", example = "45")
    private int totalCount;
    @Schema(description = "전체 페이지", example = "3")
    private int totalPages;
    @Schema(description = "현재 보고 있는 페이지", example = "1")
    private int currentPage;
    @Schema(description = "한 페이지에 들어갈 장소 수", example = "15")
    private int pageSize;
    @JsonProperty("isEnd")
    @Schema(description = "현재 페이지가 마지막 페이지인지 여부", example = "false")
    private boolean isEnd;
    @Schema(description = "근처 장소 정보(카카오 API) 리스트")
    private List<PlaceReadResponse> places;
}