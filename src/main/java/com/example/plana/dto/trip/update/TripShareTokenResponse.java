package com.example.plana.dto.trip.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 공유 토큰 응답")
public class TripShareTokenResponse {
    @Schema(description = "여행 ID", example = "T1")
    private String tripId;
    @Schema(description = "갱신된 ShareToken", example = "6fc765c7275f491***********")
    private String shareToken;
}
