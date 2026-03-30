package com.example.plana.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegionDto {
    private String regionId;
    private int zdoCode;
    private String zdoName;
    private int siguCode;
    private String siguName;
    private String createDate;
    private String latestDate;
    private String status;
}
