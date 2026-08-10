package com.example.plana.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "등록할 포인트 정보")
public class PointSave {
    @Schema(description = "포인트 ID", example = "null")
    private String pointId;
    @Schema(description = "회원 ID", example = "M1")
    private String memberId;
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
    @Schema(description = "관련 포인트 ID", example = "null")
    private String originPointId;
}
