package com.example.plana.dto.region.read;

import com.example.plana.dto.area.read.MapPos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiguResponse {
    private String regionId;
    private int siguCode;
    private String siguName;
    private MapPos mapPos;
    private String createDate;
    private String latestDate;
    private String status;
}
