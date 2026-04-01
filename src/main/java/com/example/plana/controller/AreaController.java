package com.example.plana.controller;


import com.example.plana.dto.area.read.AreaReadRequest;
import com.example.plana.dto.area.read.AreaReadResponse;
import com.example.plana.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/area")
public class AreaController {
    private final AreaService areaService;

    @GetMapping
    public AreaReadResponse getArea(@RequestParam(required = false) String regionId,
                                    @RequestParam(required = false) String zdo){
        return areaService.getArea(regionId, zdo);
    }
}
