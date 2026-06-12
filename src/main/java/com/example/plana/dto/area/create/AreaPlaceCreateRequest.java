package com.example.plana.dto.area.create;

import com.example.plana.dto.area.read.MapPos;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
/*
 * BookmarkCreateRequest에 포함될 데이터 클래스
 */
@Schema(description = "북마크 생성 시 신규 등록할 장소 요청")
public class AreaPlaceCreateRequest {
    @Schema(description = "장소 ID", example = "null", nullable = true)
    private String areaId;  // 생성 후 반환값을 담을 변수
    @Schema(description = "맵 데이터 장소 고유 ID", example = "null", nullable = true)
    private String placeId;
    @Schema(description = "장소 지역 코드", example = "29000")
    private String regionId;
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
    @Schema(description = "UI에 표시되지 않는 장소 설명", example = "출처: 공공데이터포털 API")
    private String description;
}
