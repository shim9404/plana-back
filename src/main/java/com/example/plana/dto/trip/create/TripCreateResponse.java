package com.example.plana.dto.trip.create;

import com.example.plana.dto.bookmark.read.BookmarkResponse;
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
@Schema(description = "여행 생성 응답")
public class TripCreateResponse {
    @Schema(description = "여행 ID", example = "T100")
    private String tripId;
    @Schema(description = "여행명", example = "세종 여행 계획")
    private String name;
    @Schema(description = "여행 시작일", example = "2026-05-22")
    private String startDate;
    @Schema(description = "여행 종료일", example = "2026-05-23")
    private String endDate;
    @Schema(description = "여행 기간(활성화 일자 수)", example = "2")
    private int activeDayCount;
    @Schema(description = "여행 지역 코드", example = "29000")
    private String regionId;
    @Schema(description = "북마크 리스트", example = "[]")
    private List<BookmarkResponse> bookmarks;
    private List<TripDayCreateResponse> days;
}
