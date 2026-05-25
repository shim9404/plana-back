package com.example.plana.dto.trip.update;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 정보 수정 요청")
public class TripInfoUpdateRequest {
    @Schema(description = "여행명", example = "세종 여행 계획")
    private String name;
    @Schema(description = "여행 지역 코드", example = "29000")
    private String regionId;
    @Schema(description = "참여 인원", example = "4")
    private int entryCount;
}
