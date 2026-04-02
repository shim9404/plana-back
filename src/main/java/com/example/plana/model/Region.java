package com.example.plana.model;


import lombok.Data;

@Data
public class Region {
    private String regionId;
    private int zdoCode;
    private String zdoName;
    private int siguCode;
    private String siguName;
    private String createDate;
    private String latestDate;
    private String status;
    private double mapX;
    private double mapY;
}
