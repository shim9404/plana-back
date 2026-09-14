package com.example.plana.dto.lounge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "허브 상세 추가 정보")
public class HubPlanInfoResponse {
    @Schema(description = "허브플랜 ID", example = "HP1")
    private String hubPlanId;

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

    // 키워드 태그는 별도 쿼리로 조회
    @Schema(description = "키워드 태그 목록")
    private List<String> keywordTags;

    @Schema(description = "게시일", example = "2026-09-05")
    private String createdAt;

}
