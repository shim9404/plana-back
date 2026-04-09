package com.example.plana.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberSave {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String role;
    private String socialType;
}
