package com.example.plana.dto.trip.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "여행 스케줄 재정렬 응답")
public class TripScheduleOrderResponse {
    @Schema(description = "여행 스케줄 ID", example = "TD100")
    private String tripScheduleId;
    @Schema(description = "여행 스케줄 정렬 순서", example = "1")
    private int indexSort;
}
