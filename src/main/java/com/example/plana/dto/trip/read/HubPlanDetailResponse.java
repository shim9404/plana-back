package com.example.plana.dto.trip.read;

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
@Schema(description = "허브 세부 조회 응답")
public class HubPlanDetailResponse {
    @Schema(description = "여행 상세 정보")
    private TripResponse tripDetail;

    @Schema(description = "좋아요 수", example = "999")
    private Integer likeCount;

    @Schema(description = "복사 수", example = "999")
    private Integer copyCount;

    @Schema(description = "현재 유저 좋아요 여부", example = "true")
    private Boolean isLiked;

    @Schema(description = "현재 유저 복사 여부", example = "false")
    private Boolean isCopied;

    @Schema(description = "작성자 닉네임", example = "여행가김")
    private String nickname;

    @Schema(description = "작성자 프로필 이미지", example = "https://...")
    private String profileImage;

    @Schema(description = "키워드 태그 목록", example = "[\"KW1\", \"KW2\"]")
    private List<String> keywordTags;
}
