package com.example.plana.dto.member.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPwUpdateRequest {
    private String currentPassword;
    private String newPassword;
}
