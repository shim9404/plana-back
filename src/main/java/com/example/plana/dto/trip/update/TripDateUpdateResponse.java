package com.example.plana.dto.trip.update;

import com.example.plana.dto.trip.create.TripDayCreateResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "여행 일정 수정 응답")
public class TripDateUpdateResponse {
    @Schema(description = "여행 ID", example = "T100")
    private String tripId;
    @Schema(description = "여행 시작일", example = "2026-05-22")
    private String startDate;
    @Schema(description = "여행 종료일", example = "2026-05-23")
    private String endDate;
    @Schema(description = "여행 기간(활성화 일자 수)", example = "2")
    private int activeDayCount;
    @Schema(description = "일정 변경으로 추가된 일자 응답")
    private List<TripDayCreateResponse> addDays;
}
