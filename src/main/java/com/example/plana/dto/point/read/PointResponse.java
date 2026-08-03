package com.example.plana.dto.point.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포인트 리스트")
public class PointResponse {
    @Schema(description = "포인트 ID", example = "P1")
    private String pointId;
    @Schema(description = "여행 ID", example = "null")
    private String tripId;
    @Schema(description = "내용", example = "회원가입 축하 포인트")
    private String content;
    @Schema(description = "타입", example = "EARN")
    private String type;
    @Schema(description = "포인트 이벤트", example = "SIGNUP")
    private String event;
    @Schema(description = "금액", example = "1000")
    private int amount;
    @Schema(description = "잔액", example = "1000")
    private int remain;
    @Schema(description = "관련 포인트 ID", example = "null")
    private String originPointId;
    @Schema(description = "생성 날짜", example = "2026.03.01 14:35:54")
    private String createDate;
}
