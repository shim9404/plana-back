package com.example.plana.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDayResponse {
    private String tripDayId;
    private int indexSort;
    private List<TripScheduleResponse> schedules;
}
