package com.example.plana.dto.area.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "여행지 연관 검색 결과(관광포털 API) 리스트")
public class RelatePlaceReadResponse {
    @Schema(description = "분류", example = "THEME")
    private String searchType;
    @Schema(description = "맞춤 테마 종류", example = "RELATION")
    private String searchTheme;
    @Schema(description = "이름", example = "농민순대")
    private String name;
    @Schema(description = "장소 분류 코드", example = "FD6")
    private String category;
    @Schema(description = "도 이름", example = "대전광역시")
    private String zdoName;
    @Schema(description = "시군구 이름", example = "중구")
    private String siguName;
    @Schema(description = "설명", example = "관광포털 여행지 검색(RELATION)")
    private String description;
}
