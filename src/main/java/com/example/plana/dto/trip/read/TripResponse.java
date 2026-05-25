package com.example.plana.dto.trip.read;

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
@Schema(description = "여행 조회 응답")
public class TripResponse {
    @Schema(description = "여행 ID", example = "T100")
    private String tripId;
    @Schema(description = "여행 소유자 ID", example = "M24")
    private String memberId;
    @Schema(description = "여행명", example = "세종 여행 계획")
    private String name;
    @Schema(description = "여행 시작일", example = "2026-05-22")
    private String startDate;
    @Schema(description = "여행 종료일", example = "2026-05-23")
    private String endDate;
    @Schema(description = "여행 지역 코드", example = "29000")
    private String regionId;
    @Schema(description = "참여 인원", example = "4")
    private int entryCount;
    @Schema(description = "여행 기간(활성화 일자 수)", example = "2")
    private int activeDayCount;
    private List<BookmarkResponse> bookmarks;
    private List<TripDayResponse> days;
}
