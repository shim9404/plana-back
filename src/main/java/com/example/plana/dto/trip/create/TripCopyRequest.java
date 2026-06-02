package com.example.plana.dto.trip.create;

import com.example.plana.dto.bookmark.create.BookmarkCopyRequest;
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
@Schema(description = "여행 복사 요청")
public class TripCopyRequest {
    @Schema(description = "회원 고유 ID", example = "M24")
    private String memberId;
    @Schema(description = "여행명", example = "세종 여행 계획")
    private String name;
    @Schema(description = "여행 시작일", example = "2026-05-22")
    private String startDate;
    @Schema(description = "여행 종료일", example = "2026-05-23")
    private String endDate;
    @Schema(description = "여행 지역 코드", example = "29000")
    private String regionId;
    private List<BookmarkCopyRequest> bookmarks;
    private List<TripDayCopyRequest> days;
}
