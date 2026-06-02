package com.example.plana.dto.trip.create;

import com.example.plana.dto.bookmark.create.BookmarkCopyRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripCopyRequest {
    private String memberId;
    private String name;
    private String startDate;
    private String endDate;
    private String regionId;
    private List<BookmarkCopyRequest> bookmarks;
    private List<TripDayCopyRequest> days;
}
