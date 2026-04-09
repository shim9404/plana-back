package com.example.plana.dto.trip.read;

import com.example.plana.dto.bookmark.read.BookmarkResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private String tripId;
    private String name;
    private String startDate;
    private String endDate;
    private List<BookmarkResponse> bookmarks;
    private List<TripDayResponse> days;
}
