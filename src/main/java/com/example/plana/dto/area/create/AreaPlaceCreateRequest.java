package com.example.plana.dto.area.create;

import com.example.plana.dto.area.read.MapPos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
/*
 * BookmarkCreateRequest에 포함될 데이터 클래스
 */
public class AreaPlaceCreateRequest {
    private String areaId;  // 생성 후 반환값을 담을 변수
    private String regionId;
    private String name;
    private MapPos mapPos;
    private String category;
    private String address;
    private String roadAddress;
    private String link;
    private String telephone;
    private String description;
}
