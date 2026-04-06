package com.example.plana.controller;

import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.trip.create.*;
import com.example.plana.dto.trip.delete.TripScheduleDeleteResponse;
import com.example.plana.dto.trip.update.TripInfoUpdateRequest;
import com.example.plana.dto.trip.update.TripScheduleUpdateRequest;
import com.example.plana.dto.trip.update.TripUpdateRequest;
import com.example.plana.dto.trip.update.TripUpdateResponse;
import com.example.plana.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.INSERT_SUCCESS, data));
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
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
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
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
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
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.INSERT_SUCCESS, data));
    }

    /**
     * addTripSchedule 여행 스케줄 신규 추가
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @param request TripScheduleCreateRequest
     * @return ResponseBody.data : TripScheduleCreateResponse
     */
    @PostMapping("/{tripId}/days/{tripDayId}/schedules")
    public ResponseEntity<ResponseBody> addTripSchedule(
            @PathVariable String tripId, @PathVariable String tripDayId, @RequestBody TripScheduleCreateRequest request) {
        TripScheduleCreateResponse data = tripService.createTripSchedule(tripDayId, request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.INSERT_SUCCESS, data));
    }

    /**
     * editTripSchedule 여행 스케줄 수정(갱신)
     * @param tripScheduleId 여행 스케줄 ID
     * @param request TripScheduleUpdateRequest
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/days/{tripDayId}/schedules/{tripScheduleId}")
    public ResponseEntity<ResponseBody> editTripSchedule(
            @PathVariable String tripScheduleId, @RequestBody TripScheduleUpdateRequest request) {
        tripService.updateTripSchedule(tripScheduleId, request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    /**
     * deleteTripSchedule 여행 스케줄 단건 삭제
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @param tripScheduleId 삭제할 여행 스케줄 ID
     * @return ResponseBody.data : TripScheduleDeleteResponse
     */
    @DeleteMapping("/{tripId}/days/{tripDayId}/schedules/{tripScheduleId}")
    public ResponseEntity<ResponseBody> deleteTripSchedule(
            @PathVariable String tripId, @PathVariable String tripDayId, @PathVariable String tripScheduleId) {
        TripScheduleDeleteResponse data = tripService.deleteTripSchedule(tripDayId, tripScheduleId);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.DELETE_SUCCESS, data));
    }
}
