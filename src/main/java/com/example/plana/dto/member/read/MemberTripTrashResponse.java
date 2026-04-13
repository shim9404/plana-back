package com.example.plana.dto.member.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTripTrashResponse {
    private String tripId;
    private String name;
    private String status;
    private String startDate;
    private String endDate;
    private String latestDate;
    private int remainDate;
    private int scheduleCount;
    private int bookmarkCount;
}
