package com.example.plana.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripMember {
    private String tripId;
    private String memberId;
    private String role;
    private String invitedAt;
    private String joinedAt;
    private String createDate;
    private String latestDate;
    private String status;
    private String invitedToken;
    private String invitedEmail;
}
