package com.example.plana.model;


import lombok.Data;

@Data
public class Region {
    public String regionId;
    public int zdoCode;
    public String zdoName;
    public int siguCode;
    public String siguName;
    public String createDate;
    public String latestDate;
    public String status;
    public double mapX;
    public double mapY;
}
