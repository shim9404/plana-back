package com.example.plana.dto.area.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "북마크 내 장소 정보 응답")
public class AreaForBookmarkResponse {
    @Schema(description = "장소명", example = "죠샌드위치 세종시청점")
    private String name;
    @Schema(description = "위치 정보", example = """
                    {
                      "x": 127.289426902537,
                      "y": 36.4811232103818
                    }
                    """)
    private MapPos mapPos;
    @Schema(description = "장소 분류 코드", example = "FD6")
    private String category;
    @Schema(description = "지번 주소", example = "세종특별자치시 보람동 707")
    private String address;
    @Schema(description = "도로명 주소", example = "세종특별자치시 시청대로 163")
    private String roadAddress;
    @Schema(description = "URL", example = "place.map.kakao.com/1649101126")
    private String link;
    @Schema(description = "전화번호", example = "000-0000-0000")
    private String telephone;
}
