package com.example.plana.dto.region.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionReadResponse {
    private String regionId;
    private String zdoName;
    private String siguName;
    private double MapX;
    private double MapY;

}
