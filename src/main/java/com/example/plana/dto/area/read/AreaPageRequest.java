package com.example.plana.dto.area.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AreaPageRequest {
    private String regionId;
    private String searchType;
    private int page;
    private int size;

    public int getOffset() {
        return (page - 1) * size;
    }

    public String getZdoCode() {
        return regionId != null ? regionId.substring(0, 2) : null;
    }
}
