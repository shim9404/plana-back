package com.example.plana.dto.trip.update;

import com.example.plana.dto.trip.create.TripDayCreateResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripDateUpdateResponse {
    private String tripId;
    private String startDate;
    private String endDate;
    private int activeDayCount;
    private List<TripDayCreateResponse> addDays;
}
