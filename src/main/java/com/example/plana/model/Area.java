package com.example.plana.model;


import com.example.plana.dto.area.read.MapPos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Area {
    private String areaId;
    private String searchType;
    private String regionId;
    private String name;
    private double mapX;
    private double mapY;
    private String category;
    private String address;
    private String roadAddress;
    private String link;
    private String telePhone;
    private String description;
    private int bookmarkCount;
    private String createDate;
    private String latestDate;
    private String status;
}
