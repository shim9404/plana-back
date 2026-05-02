package com.example.plana.dto.area.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceReadPageResponse {
    private int totalCount;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    @JsonProperty("isEnd")
    private boolean isEnd;
    private List<PlaceReadResponse> places;
}