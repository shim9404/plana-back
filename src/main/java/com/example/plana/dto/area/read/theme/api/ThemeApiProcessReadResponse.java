package com.example.plana.dto.area.read.theme.api;

import com.example.plana.dto.area.read.theme.RelatePlaceReadResponse;
import com.example.plana.dto.area.read.theme.ThemeReadResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "맞춤 테마의 여행지(관광포털 API) 응답 가공 결과")
public class ThemeApiProcessReadResponse {
    @Schema(description = "총 검색 결과 개수", example = "45")
    private int totalCount;
    @Schema(description = "맞춤 테마의 여행지(관광포털 API) 결과 리스트")
    private List<ThemeReadResponse> themeList;
    @Schema(description = "연관 여행지(관광포털 API) 결과 리스트")
    private List<RelatePlaceReadResponse> relatedList;
}
