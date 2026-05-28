package com.example.plana.dto.area.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "근처 장소 정보(카카오 API) 리스트")
public class PlaceReadResponse {
    @Schema(description = "근처 장소 kakao map id", example = "191240179")
    private String placeId;
    @Schema(description = "Area Table에 등록된 id (없으면 null)", example = "null")
    private String areaId;
    @Schema(description = "분류", example = "PLACE")
    private String searchType;
    @Schema(description = "이름", example = "곰순이꼬마김밥")
    private String name;
    @Schema(description = "위치 정보", example = """
                    {
                      "x": 127.38226876784677,
                      "y": 36.3497631135529
                    }
                    """)
    private MapPos mapPos;
    @Schema(description = "장소 분류 코드", example = "FD6")
    private String category;
    @Schema(description = "지번 주소", example = "대전 서구 둔산동 1212")
    private String address;
    @Schema(description = "도로명 주소", example = "대전 서구 둔산로74번길 31")
    private String roadAddress;
    @Schema(description = "링크", example = "http://place.map.kakao.com/191240179")
    private String link;
    @Schema(description = "전화번호", example = "042-477-3412")
    private String telePhone;
    @Schema(description = "설명", example = "카카오개발자센터 장소 검색")
    private String description;
}
