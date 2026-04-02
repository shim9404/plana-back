package com.example.plana.dto.trip.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDayUpdateResponse {
    private String tripDayId;
    private int indexSort;
    private List<TripScheduleUpdateResponse> schedules;
}
