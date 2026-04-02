package com.example.plana.dto.trip.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDayCreateResponse {
    private String tripDayId;
    private int indexSort;
    private List<TripScheduleCreateResponse> schedules;
}
