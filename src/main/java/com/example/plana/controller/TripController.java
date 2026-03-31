package com.example.plana.controller;

import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.TripRequest;
import com.example.plana.dto.TripResponse;
import com.example.plana.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    /**
     * generateTrip 여행 신규 생성 호출 함수
     * @param request TripRequest
     * @return ResponseBody.data : TripResponse
     */
    @PostMapping
    public ResponseEntity<ResponseBody> generateTrip(@RequestBody TripRequest request) {

        TripResponse data = tripService.createTrip(request);

        ResponseBody response = ResponseBody.builder()
                .success(true)
                .code(201)
                .message("Created")
                .data(Map.of("trip", data))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
