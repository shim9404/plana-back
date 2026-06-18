package com.example.plana.dto.lounge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "허브플랜 단건 응답")
public class HubPlanReadResponse {
    @Schema(description = "허브플랜 ID", example = "HP1")
    private String hubPlanId;

    @Schema(description = "여행 ID", example = "T1")
    private String tripId;

    @Schema(description = "멤버 ID", example = "M1")
    private String memberId;

    @Schema(description = "좋아요 수", example = "10")
    private Integer likeCount;

    @Schema(description = "복사 수", example = "5")
    private Integer copyCount;

    @Schema(description = "생성일", example = "2024-01-01 00:00:00")
    private String createdDate;
}