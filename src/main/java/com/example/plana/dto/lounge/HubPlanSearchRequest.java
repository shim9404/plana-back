package com.example.plana.dto.lounge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "허브 검색 요청")
public class HubPlanSearchRequest {
    @Schema(description = "작성자 닉네임 검색", example = "여행가")
    private String nickname;

    @Schema(description = "여행 제목(name) 키워드 검색", example = "대전")
    private String name;

    @Schema(description = "지역 ID 목록(합집합)", example = "[\"30000\"]")
    private List<String> regionIds;

    @Schema(description = "최소 박 수(1박 2일 → 1)", example = "1")
    private Integer minNights;

    @Schema(description = "최대 박 수(1박 2일 → 1)", example = "5")
    private Integer maxNights;

    @Schema(description = "태그 키워드 ID 목록", example = "[\"KW1\", \"KW2\"]")
    private List<String> keywordIds;

    @Schema(description = "정렬 기준 (LATEST, LIKE, COPY)", example = "LATEST")
    private String sortBy;

    @Schema(description = "현재 페이지", example = "1")
    private int page = 1;

    @Schema(description = "페이지당 항목 수", example = "10")
    private int size = 10;

    // Service에서 채워 넣는 필드
    @JsonIgnore
    @Schema(hidden = true)
    private List<String> exactRegionIds;
    @JsonIgnore
    @Schema(hidden = true)
    private List<Integer> zdoCodes;

    public int getOffset() {
        return (page - 1) * size;
    }

    public int getKeywordCount() {
        return keywordIds != null ? keywordIds.size() : 0;
    }

    public int getExactRegionCount() {
        return exactRegionIds != null ? exactRegionIds.size() : 0;
    }

    public int getZdoCodeCount() {
        return zdoCodes != null ? zdoCodes.size() : 0;
    }
}