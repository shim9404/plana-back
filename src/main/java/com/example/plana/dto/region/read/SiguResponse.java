package com.example.plana.dto.region.read;

import com.example.plana.dto.area.read.MapPos;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "시군구 단위 응답")
public class SiguResponse {
    @Schema(description = "행정 구역 ID", example = "32030")
    private String regionId;
    @Schema(description = "시군구 코드", example = "030")
    private int siguCode;
    @Schema(description = "시군구 이름", example = "강릉시")
    private String siguName;
    @Schema(description = "위치 정보", example = """
                    {
                      "x": 128.8761,
                      "y": 37.7519
                    }
                    """)
    private MapPos mapPos;
    @Schema(description = "생성 날짜", example = "2026-03-27 06:26:03")
    private String createDate;
    @Schema(description = "수정 날짜", example = "2026-04-07 09:53:05")
    private String latestDate;
    @Schema(description = "상태", example = "ACTIVE")
    private String status;
}
