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
@Schema(description = "여행 공동 작업 초대 수락 응답")
public class TripInviteAcceptResponse {
    @Schema(description = "초대 받은 여행 ID", example = "T100")
    private String tripId;
    @Schema(description = "초대 수락 회원ID", example = "M1")
    private String memberId;
    @Schema(description = "초대 수락 회원의 권한", example = "EDITOR")
    private String role;
}
