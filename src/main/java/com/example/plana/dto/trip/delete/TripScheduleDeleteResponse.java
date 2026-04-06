package com.example.plana.dto.trip.delete;

import com.example.plana.dto.trip.read.TripScheduleOrderResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripScheduleDeleteResponse {
    private String tripDayId;
    private List<TripScheduleOrderResponse> scheduleOrders;
}
