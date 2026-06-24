package com.example.plana.dto.trip.invite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 공동 작업 초대 수락 요청")
public class TripInviteAcceptRequest {
    @Schema(description = "여행 초대 토큰", example = "6fc765c7275f491***********")
    private String inviteToken;
    @Schema(description = "초대장 수신 이메일", example = "a@a.com")
    private String email;
}
