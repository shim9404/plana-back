package com.example.plana.dto.trip.create;

import com.example.plana.dto.bookmark.create.BookmarkCreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDayCopyRequest {
    @Schema(description = "여행 일자 ID", example = "TD100")
    private String tripDayId;
    @Schema(description = "여행 일자 정렬 순서(N일차)", example = "1")
    private int indexSort;
    private List<BookmarkCreateRequest> bookmarks;
    private List<TripScheduleCopyRequest> schedules;
}
