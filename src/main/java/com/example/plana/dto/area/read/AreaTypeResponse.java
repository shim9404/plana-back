package com.example.plana.dto.area.read;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaTypeResponse {
    private String searchType;
    private int areaCount;
    private List<AreaDetailResponse> areas;
}


