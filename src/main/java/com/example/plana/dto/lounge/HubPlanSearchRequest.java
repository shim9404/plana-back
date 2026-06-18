package com.example.plana.dto.lounge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "허브플랜 검색 요청")
public class HubPlanSearchRequest {
    @Schema(description = "멤버 ID로 검색", example = "M1")
    private String memberId;

    @Schema(description = "정렬 기준 (LATEST: 최신순, LIKE: 좋아요순, COPY: 복사순)", example = "LATEST")
    private String sortBy;

    @Schema(description = "현재 페이지 (1부터 시작)", example = "1")
    private Integer page = 1;

    @Schema(description = "페이지당 항목 수", example = "10")
    private Integer size = 10;

    public int getOffset() {
        return (page - 1) * size;
    }
}