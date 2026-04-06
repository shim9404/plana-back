package com.example.plana.dto.trip.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripScheduleCreateRequest {
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
