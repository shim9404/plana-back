package com.example.plana.controller;

import com.example.plana.dto.RegionResponse;
import com.example.plana.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/regions")
public class RegionController {
    private final RegionService regionService;

    /**
     * 행정구역 정보 반환
     * @return RegionResponse
     */
    @GetMapping
    public RegionResponse region(){
        return regionService.readRegion();
    }
}
