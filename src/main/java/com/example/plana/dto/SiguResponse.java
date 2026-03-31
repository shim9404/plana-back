package com.example.plana.dto;

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
    private String createDate;
    private String latestDate;
    private String status;
}
