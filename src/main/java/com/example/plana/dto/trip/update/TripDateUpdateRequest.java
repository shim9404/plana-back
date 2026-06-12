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
@Schema(description = "여행 일정 수정 요청")
public class TripDateUpdateRequest {
    @Schema(description = "여행 시작일", example = "2026-05-22")
    private String startDate;
    @Schema(description = "여행 종료일", example = "2026-05-23")
    private String endDate;
}
