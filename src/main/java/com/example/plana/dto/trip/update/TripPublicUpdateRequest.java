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
@Schema(description = "여행 공개 갱신 요청")
public class TripPublicUpdateRequest {
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;
}
