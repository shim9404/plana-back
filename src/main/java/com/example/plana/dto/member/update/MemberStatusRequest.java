package com.example.plana.dto.member.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatusRequest {
    private String email;
    private String name;
    private String password;
}
