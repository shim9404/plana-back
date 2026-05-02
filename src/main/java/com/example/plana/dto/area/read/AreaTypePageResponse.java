package com.example.plana.dto.area.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaTypePageResponse {
    private String regionId;
    private AreaTypeResponse data;
}
