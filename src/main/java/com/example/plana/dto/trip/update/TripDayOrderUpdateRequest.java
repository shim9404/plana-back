package com.example.plana.dto.trip.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "여행 일자 재정렬 요청")
public class TripDayOrderUpdateRequest {
    private List<DayOrder> dayOrders;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "여행 일자 재정렬 요청값")
    public static class DayOrder {
        @Schema(description = "여행 일자 ID", example = "TD100")
        private String tripDayId;
        @Schema(description = "여행 일자 정렬 순서(N일차)", example = "1")
        private int indexSort;
    }
}
