package com.example.plana.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripRequest {
    private String memberId;
    private String name;
    private String startDate;
    private String endDate;
    private String status;
}
