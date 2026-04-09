package com.example.plana.dto.area.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaForBookmarkResponse {
    private String name;
    private MapPos mapPos;
    private String category;
    private String address;
    private String roadAddress;
    private String link;
    private String telephone;
}
