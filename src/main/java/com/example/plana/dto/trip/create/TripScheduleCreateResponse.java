package com.example.plana.dto.trip.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripScheduleCreateResponse {
    private String tripScheduleId;
    private int indexSort;
}
