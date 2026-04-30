package com.example.plana.dto.member.update;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatusUpdateRequest {
    private String memberId;
    private String status;
}
