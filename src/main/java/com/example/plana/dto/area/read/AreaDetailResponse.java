package com.example.plana.dto.area.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "근처 장소 정보(장소 DB) 리스트")
public class AreaDetailResponse {
    @Schema(description = "Area Table에 등록된 id", example = "A4808")
    private String areaId;
    @Schema(description = "이름", example = "계곡가든꽃게장")
    private String name;
    @Schema(description = "위치 정보", example = """
                    {
                      "x": 0,
                      "y": 0
                    }
                    """)
    private MapPos mapPos;
    @Schema(description = "장소 분류 코드", example = "FD6")
    private String category;
    @Schema(description = "지번 주소", example = "null")
    private String address;
    @Schema(description = "도로명 주소", example = "전북 군산시 개정면 금강로 470")
    private String roadAddress;
    @Schema(description = "링크", example = "null")
    private String link;
    @Schema(description = "전화번호", example = "063-453-0608")
    private String telePhone;
    @Schema(description = "설명", example = "공공데이터포털 전북특별자치도_ 음식문화정보 발췌색")
    private String description;
    @Schema(description = "북마크 수", example = "0")
    private int bookmarkCount;
    @Schema(description = "생성 날짜", example = "2026-03-27 06:26:03")
    private String createDate;
    @Schema(description = "수정 날짜", example = "2026-04-27 07:40:27")
    private String latestDate;
    @Schema(description = "상태", example = "ACTIVE")
    private String status;
}
