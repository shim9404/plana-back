package com.example.plana.dto.trip.update;

import com.example.plana.dto.bookmark.create.BookmarkCreateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripUpdateRequest {
    private String memberId;
    private String name;
    private String startDate;
    private String endDate;
    private List<TripDayUpdateRequest> days;
}
