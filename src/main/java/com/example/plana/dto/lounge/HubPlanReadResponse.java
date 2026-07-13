package com.example.plana.dto.lounge;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Schema(description = "허브 단건 응답")
public class HubPlanReadResponse {
    @Schema(description = "허브플랜 ID", example = "HP1")
    private String hubPlanId;

    @Schema(description = "여행 ID", example = "T1")
    private String tripId;

    @Schema(description = "여행 제목", example = "대전 1박2일")
    private String tripName;

    @Schema(description = "작성자 멤버 ID", example = "M1")
    private String memberId;

    @Schema(description = "작성자 닉네임", example = "여행가")
    private String nickname;

    @Schema(description = "좋아요 수", example = "999")
    private Integer likeCount;

    @Schema(description = "복사 수", example = "999")
    private Integer copyCount;

    @Schema(description = "생성일", example = "2024-01-01 00:00:00")
    private String createdDate;

    @Schema(description = "여행 박 수", example = "3")
    private Integer nights;

    @Schema(description = "키워드 ID 목록", example = "[\"KW1\", \"KW2\"]")
    private List<String> keywordIds;

    @Schema(description = "카테고리 비율 목록")
    private List<CategoryStatResponse> categoryStatList;

    @Schema(description = "지역 비율 목록")
    private List<RegionStatResponse> regionStatList;

    // 내부용 raw 필드 (API 응답 제외)
    @JsonIgnore
    @Schema(hidden = true)
    private String rawKeywordIds;
}