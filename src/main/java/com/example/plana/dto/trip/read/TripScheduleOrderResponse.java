package com.example.plana.dto.trip.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripScheduleOrderResponse {
    private String tripScheduleId;
    private int indexSort;
}
