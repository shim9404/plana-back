package com.example.plana.dto.area.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "맞춤 테마의 여행지(관광포털 API) 리스트")
public class ThemeReadResponse {
    @Schema(description = "여행지 콘텐츠 id", example = "2755034")
    private String themeId;
    @Schema(description = "Area Table에 등록된 id (없으면 null)", example = "null")
    private String areaId;
    @Schema(description = "분류", example = "Theme")
    private String searchType;
    @Schema(description = "맞춤 테마 종류", example = "PET")
    private String searchTheme;
    @Schema(description = "이름", example = "바람개비 마을")
    private String name;
    @Schema(description = "위치 정보", example = """
                    {
                      "x": 126.921387595,
                      "y": 36.1307381717
                    }
                    """)
    private MapPos mapPos;
    @Schema(description = "장소 분류 코드", example = "AT4")
    private String category;
    @Schema(description = "지번 주소", example = "null")
    private String address;
    @Schema(description = "도로명 주소", example = "전북특별자치도 익산시 성당면 성당로 762")
    private String roadAddress;
    @Schema(description = "링크", example = "null")
    private String link;
    @Schema(description = "전화번호", example = "null")
    private String telePhone;
    @Schema(description = "설명", example = "관광포털 여행지 검색(PET)")
    private String description;
}
