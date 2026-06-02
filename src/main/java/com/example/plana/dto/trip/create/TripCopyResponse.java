package com.example.plana.dto.trip.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 복사 응답")
public class TripCopyResponse {
    @Schema(description = "여행 ID", example = "T100")
    private String tripId;
    @Schema(description = "여행명", example = "세종 여행 계획")
    private String name;
}
