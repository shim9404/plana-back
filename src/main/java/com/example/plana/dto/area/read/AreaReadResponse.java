package com.example.plana.dto.area.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaReadResponse {
    private int totalCount;
    private String regionId;
    private AreaTypeResponse place;
    private AreaTypeResponse spot;
    private AreaTypeResponse food;
}
