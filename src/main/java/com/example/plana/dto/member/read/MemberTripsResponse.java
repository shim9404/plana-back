package com.example.plana.dto.member.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 여행 목록 조회 응답")
public class MemberTripsResponse {
    @Schema(description = "회원 ID", example = "M6")
    private String memberId;
    @Schema(description = "여행 목록 리스트")
    private List<MemberTripResponse> Trips;
}
