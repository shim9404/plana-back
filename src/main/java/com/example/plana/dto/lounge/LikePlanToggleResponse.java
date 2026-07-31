package com.example.plana.dto.lounge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "좋아요 토글 응답")
public class LikePlanToggleResponse {
    @Schema(description = "허브플랜 ID", example = "HP1")
    private String hubPlanId;

    @Schema(description = "현재 좋아요 여부", example = "true")
    private Boolean isLiked;

    @Schema(description = "변경된 좋아요 수", example = "10")
    private Integer likeCount;
}