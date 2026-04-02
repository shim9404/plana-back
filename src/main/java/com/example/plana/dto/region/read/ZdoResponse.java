package com.example.plana.dto.region.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ZdoResponse {
    private int zdoCode;
    private String zdoName;
    private List<SiguResponse> sigus;
}
