package com.example.plana.dto.trip.update;


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
@Schema(description = "여행 공개 갱신 요청")
public class TripPublicUpdateRequest {
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;

    @Schema(description = "선택된 키워드 ID 리스트", example = "[\"KW1\", \"KW4\"]")
    private List<String> keywords;
}