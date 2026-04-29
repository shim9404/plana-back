package com.example.plana.dto.area.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceReadResponse {
    private String placeId;
    private String areaId;
    private String searchType;
    private String name;
    private MapPos mapPos;
    private String category;
    private String address;
    private String roadAddress;
    private String link;
    private String telePhone;
    private String description;
}
