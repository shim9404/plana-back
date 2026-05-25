package com.example.plana.dto.trip.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 스케줄 생성 응답")
public class TripScheduleCreateResponse {
    @Schema(description = "여행 스케줄 ID", example = "TS1234")
    private String tripScheduleId;
    @Schema(description = "여행 일자 ID", example = "TD100")
    private String tripDayId;
    @Schema(description = "여행 스케줄 정렬 순서", example = "1")
    private int indexSort;
    @Schema(description = "시작 시간", nullable = true, example = "null")
    private String startTime;
    @Schema(description = "종료 시간", nullable = true, example = "null")
    private String endTime;
    @Schema(description = "연결된 북마크 ID", nullable = true, example = "null")
    private String bookmarkId;
    @Schema(description = "장소명", nullable = true, example = "null")
    private String context;
    @Schema(description = "구분", nullable = true, example = "null")
    private String category;
    @Schema(description = "예산", nullable = true, example = "null")
    private Integer price;
    @Schema(description = "메모", nullable = true, example = "null")
    private String memo;
    @Schema(description = "URL", nullable = true, example = "null")
    private String link;
}
