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
@Schema(description = "여행 스케줄 수정 요청")
public class TripScheduleCopyRequest {
    @Schema(description = "여행 스케줄 ID", example = "TD100")
    private String tripScheduleId;
    @Schema(description = "여행 스케줄 정렬 순서", example = "1")
    private int indexSort;
    @Schema(description = "시작 시간", example = "14:30")
    private String startTime;
    @Schema(description = "종료 시간", example = "16:30")
    private String endTime;
    @Schema(description = "연결된 북마크 ID", example = "BM123")
    private String bookmarkId;
    @Schema(description = "장소명", example = "죠샌드위치 세종시청점")
    private String context;
    @Schema(description = "구분", example = "식사")
    private String category;
    @Schema(description = "예산", example = "15000")
    private Integer price;
    @Schema(description = "메모", example = "2인 아침식사 가격 책정")
    private String memo;
    @Schema(description = "URL", example = "place.map.kakao.com/1649101126")
    private String link;
}
