package com.example.plana.dto.trip.read;

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
@Schema(description = "여행 일자 조회 응답")
public class TripDayResponse {
    @Schema(description = "여행 일자 ID", example = "TD100")
    private String tripDayId;
    @Schema(description = "여행 일자 정렬 순서(N일차)", example = "1", type = "int")
    private int indexSort;
    private List<TripScheduleResponse> schedules;
}
