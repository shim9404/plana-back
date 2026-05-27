package com.example.plana.dto.member.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "여행 목록 리스트")
public class MemberTripResponse {
    @Schema(description = "여행 ID", example = "T35")
    private String tripId;
    @Schema(description = "여행명", example = "제주 1박2일")
    private String name;
    @Schema(description = "여행 상태 (활성화, 휴지통)", example = "ACTIVE")
    private String status;
}
