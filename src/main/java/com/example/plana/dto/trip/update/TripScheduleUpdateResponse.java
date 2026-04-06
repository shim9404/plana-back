package com.example.plana.dto.trip.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripScheduleUpdateResponse {
    private String tripScheduleId;
    private String tripDayId;
    private int indexSort;
    private String startTime;
    private String endTime;
    private String bookmarkId;
    private String context;     // bookmarkId가 있을 경우, 장소명 담아서 반환
    private String category;
    private Integer price;
    private String memo;
    private String link;
}
