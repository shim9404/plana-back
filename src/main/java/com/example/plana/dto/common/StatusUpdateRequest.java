package com.example.plana.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상태 수정 요청")
public class StatusUpdateRequest {
    @Schema(description = "상태 (ACTIVE/INACTIVE/DELETED)", example = "ACTIVE")
    private String status;
}
