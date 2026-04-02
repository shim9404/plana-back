package com.example.plana.dto.trip.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripScheduleUpdateRequest {
    private String tripScheduleId;
    private int indexSort;
    private String startTime;
    private String endTime;
    private String bookmarkId;
    private String context;
    private String category;
    private Integer price;
    private String memo;
    private String link;
}
