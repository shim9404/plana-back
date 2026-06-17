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
@Schema(description = "허브 공개 여부 갱신 응답")
public class UpdateHubPlanPublicResponse {
    @Schema(description = "허브 게시물 id", example ="HP1")
    private String hubPlanId;
}
