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
public class TripDayUpdateRequest {
    private String tripDayId;
    private String tripId;
    private int indexSort;
    private List<TripScheduleUpdateRequest> schedules;
}
