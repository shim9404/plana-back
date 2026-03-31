package com.example.plana.dto.trip.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripCreateRequest {
    private String memberId;
    private String name;
    private String startDate;
    private String endDate;
    private String status;
}
