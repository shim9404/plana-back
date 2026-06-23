package com.example.plana.dto.lounge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행의 카테고리 비율 반환")
public class CategoryStatResponse {
    @Schema(description = "카테고리 코드", example = "FD6")
    private String category;

    @Schema(description = "비율 (%)", example = "100")
    private Integer ratio;
}