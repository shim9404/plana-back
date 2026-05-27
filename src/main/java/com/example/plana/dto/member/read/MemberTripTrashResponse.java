package com.example.plana.dto.member.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비활성화된 여행 목록 리스트")
public class MemberTripTrashResponse {
    @Schema(description = "여행 ID", example = "T675")
    private String tripId;
    @Schema(description = "여행명", example = "여행을 떠나요")
    private String name;
    @Schema(description = "여행 상태 (활성화, 휴지통)", example = "INACTIVE")
    private String status;
    @Schema(description = "여행 시작 일자", example = "2026-06-01")
    private String startDate;
    @Schema(description = "여행 종료 일자", example = "2026-06-02")
    private String endDate;
    @Schema(description = "여행 정보 수정 일자", example = "2026-05-20 07:57:19")
    private String latestDate;
    @Schema(description = "남은 보관 일(30일 기준)", example = "24")
    private int remainDate;
    @Schema(description = "여행 계획 목록 수", example = "6")
    private int scheduleCount;
    @Schema(description = "북마크 목록 수", example = "3")
    private int bookmarkCount;
}
