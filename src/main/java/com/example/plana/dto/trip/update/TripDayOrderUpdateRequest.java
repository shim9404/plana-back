package com.example.plana.dto.trip.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripDayOrderUpdateRequest {
    private List<DayOrder> dayOrders;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayOrder {
        private String tripDayId;
        private int indexSort;
    }
}
