package com.example.plana.controller;

import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.trip.create.TripCreateRequest;
import com.example.plana.dto.trip.create.TripCreateResponse;
import com.example.plana.dto.trip.create.TripDayCreateRequest;
import com.example.plana.dto.trip.create.TripDayCreateResponse;
import com.example.plana.dto.trip.update.TripInfoUpdateRequest;
import com.example.plana.dto.trip.update.TripUpdateRequest;
import com.example.plana.dto.trip.update.TripUpdateResponse;
import com.example.plana.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ResponseBody> generateTrip(@RequestBody TripCreateRequest request) {
        TripCreateResponse data = tripService.createTrip(request);

        ResponseBody response = ResponseBody.builder()
                .success(true)
                .code(201)
                .message("Created")
                .data(Map.of("trip", data))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * saveTrip 여행 전체 저장
     * 작동 시나리오 : 네트워크 에러 등의 사유로 편집 시 자동 저장이 이루어지지 않았을 경우, 저장 시점에 기존 데이터를 전부 삭제한 뒤 재생성한다.
     * @param tripId 여행 ID
     * @param request TripUpdateRequest
     * @return ResponseBody.data : TripUpdateResponse
     */
    @PutMapping("/{tripId}")
    public ResponseEntity<ResponseBody> saveTrip(@PathVariable String tripId, @RequestBody TripUpdateRequest request) {
        TripUpdateResponse data = tripService.saveTrip(tripId, request);

        ResponseBody response = ResponseBody.builder()
                .success(true)
                .code(200)
                .message("OK")
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * editTripInfo 여행 정보(시작 일자, 종료 일자, 여행명) 갱신
     * @param tripId 여행 ID
     * @param request TripInfoUpdateRequest
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/info")
    public ResponseEntity<ResponseBody> editTripInfo(@PathVariable String tripId, @RequestBody TripInfoUpdateRequest request) {
        tripService.updateTripInfo(tripId, request);

        ResponseBody response = ResponseBody.builder()
                .success(true)
                .code(204)
                .message("No Content")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * addTripDay 여행 일자 신규 추가
     * @param tripId 여행 ID
     * @param request TripDayCreateRequest
     * @return ResponseBody.data : TripDayCreateResponse
     */
    @PostMapping("/{tripId}/days")
    public ResponseEntity<ResponseBody> addTripDay(@PathVariable String tripId, @RequestBody TripDayCreateRequest request) {
        TripDayCreateResponse data = tripService.createTripDay(tripId, request);

        ResponseBody response = ResponseBody.builder()
                .success(true)
                .code(201)
                .message("Created")
                .data(Map.of("trip", data))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
