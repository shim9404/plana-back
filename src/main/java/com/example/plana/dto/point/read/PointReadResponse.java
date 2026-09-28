package com.example.plana.dto.point.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 포인트 정보 조회 응답")
public class PointReadResponse {
    @Schema(description = "회원 ID", example = "M6")
    private String memberId;
    @Schema(description = "포인트 리스트")
    private List<PointResponse> points;
}
