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
@Schema(description = "여행 공동 작업 초대 요청")
public class TripInviteRequest {
    @Schema(description = "초대장 수신 이메일", example = "a@a.com")
    private String invitedEmail;
    @Schema(description = "초대할 회원의 권한", example = "EDITOR")
    private String role;
}
