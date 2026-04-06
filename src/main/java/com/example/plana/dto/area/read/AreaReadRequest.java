package com.example.plana.dto.area.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AreaReadRequest {
    private String regionId;
    // 근처 장소 DB 파악용
    private double mapX;
    private double mapY;
}
