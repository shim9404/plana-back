package com.example.plana.dto.region.read;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "도 단위 응답")
public class ZdoResponse {
    @Schema(description = "도 코드", example = "32")
    private int zdoCode;
    @Schema(description = "도 이름", example = "강원특별자치도")
    private String zdoName;
    private List<SiguResponse> sigus;
}
