package com.example.plana.controller;

import com.example.plana.auth.CustomUserDetails;
import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.bookmark.create.BookmarkCreateRequest;
import com.example.plana.dto.bookmark.create.BookmarkCreateResponse;
import com.example.plana.dto.bookmark.read.BookmarkResponse;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.common.StatusUpdateRequest;
import com.example.plana.dto.trip.create.*;
import com.example.plana.dto.trip.read.TripResponse;
import com.example.plana.dto.trip.update.*;
import com.example.plana.service.BookmarkService;
import com.example.plana.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final BookmarkService bookmarkService;

    /**
     * generateTrip 여행 신규 생성 호출 함수
     * @param request TripRequest
     * @return ResponseBody.data : TripResponse
     */
    @PostMapping
    public ResponseEntity<ResponseBody> generateTrip(@RequestBody TripCreateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        TripCreateResponse data = tripService.createTrip(request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.INSERT_SUCCESS, data));
    }

    /**
     * getTrip 여행 정보 조회(단건/상세)
     * @param tripId 여행 ID
     * @return ResponseBody.data : TripResponse
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<ResponseBody> getTrip(@PathVariable String tripId, @AuthenticationPrincipal CustomUserDetails principal) {
        TripResponse data = tripService.readTrip(tripId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.SELECT_SUCCESS, data));
    }

    /**
     * saveTrip 여행 전체 저장
     * 작동 시나리오 : 네트워크 에러 등의 사유로 편집 시 자동 저장이 이루어지지 않았을 경우, 저장 시점에 기존 데이터를 전부 삭제한 뒤 재생성한다.
     * @param tripId 여행 ID
     * @param request TripUpdateRequest
     * @return ResponseBody.data : TripUpdateResponse
     */
    @PutMapping("/{tripId}")
    public ResponseEntity<ResponseBody> saveTrip(@PathVariable String tripId, @RequestBody TripUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        TripUpdateResponse data = tripService.saveTrip(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
    }

    /**
     * editTripInfo 여행 정보(시작 일자, 종료 일자, 여행명) 갱신
     * @param tripId 여행 ID
     * @param request TripInfoUpdateRequest
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/info")
    public ResponseEntity<ResponseBody> editTripInfo(@PathVariable String tripId, @RequestBody TripInfoUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.updateTripInfo(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    /**
     * editTripInfo 여행 일자(시작 일자, 종료 일자) 갱신 후 부족한 일자 생성 및 반환
     * @param tripId 여행 ID
     * @param request TripDateUpdateRequest
     * @return ResponseBody.data : TripDateUpdateResponse
     */
    @PatchMapping("/{tripId}/date")
    public ResponseEntity<ResponseBody> editTripDate(@PathVariable String tripId, @RequestBody TripDateUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        TripDateUpdateResponse data = tripService.updateTripDate(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
    }

    /**
     * editTripStatus 여행 상태(ACTIVE/INACTIVE/DELETED) 갱신
     * @param tripId 여행 ID
     * @param request TripStatusUpdateResponse
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/status")
    public ResponseEntity<ResponseBody> editTripStatus(@PathVariable String tripId, @RequestBody StatusUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {

        log.info("receive api");
        tripService.updateTripStatus(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    /**
     * deleteTrip 여행 단건 삭제 - 하위 일자 및 스케줄 전체 삭제
     * @param tripId 여행 ID
     * @return ResponseBody.data : null
     */
    @DeleteMapping("/{tripId}")
    public ResponseEntity<ResponseBody> deleteTrip(@PathVariable String tripId, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.deleteTrip(tripId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.DELETE_SUCCESS, null));
    }

    /**
     * addTripDay 여행 일자 신규 추가
     * @param tripId 여행 ID
     * @return ResponseBody.data : TripDayCreateResponse
     */
    @SuppressWarnings("unused")
    @PostMapping("/{tripId}/days")
    public ResponseEntity<ResponseBody> addTripDay(@PathVariable String tripId, @AuthenticationPrincipal CustomUserDetails principal) {
        TripDayCreateResponse data = tripService.createTripDay(tripId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.INSERT_SUCCESS, data));
    }

    /**
     * deleteTripDay 여행 일자 삭제(여행 일자 하위 스케줄 포함)
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @return ResponseBody.data : null
     */
    @DeleteMapping("/{tripId}/days/{tripDayId}")
    public ResponseEntity<ResponseBody> deleteTripDay(@PathVariable String tripId, @PathVariable String tripDayId, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.deleteTripDay(tripId, tripDayId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.DELETE_SUCCESS, null));
    }

    /**
     * reorderTripDays 여행 일자 재정렬(전체 일괄)
     * @param tripId 여행 ID
     * @param request TripDayOrderUpdateRequest
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/days/reorder")
    public ResponseEntity<ResponseBody> reorderTripDays(@PathVariable String tripId, @RequestBody TripDayOrderUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.updateTripDaysIndexSort(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    /**
     * addTripSchedule 여행 스케줄 신규 추가
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @return ResponseBody.data : TripScheduleCreateResponse
     */
    @PostMapping("/{tripId}/days/{tripDayId}/schedules")
    public ResponseEntity<ResponseBody> addTripSchedule(
            @PathVariable String tripId, @PathVariable String tripDayId, @AuthenticationPrincipal CustomUserDetails principal) {
        TripScheduleCreateResponse data = tripService.createTripSchedule(tripId, tripDayId, principal.getMemberId());
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
            @PathVariable String tripId, @PathVariable String tripScheduleId, @RequestBody TripScheduleUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.updateTripSchedule(tripId, tripScheduleId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    /**
     * reorderTripSchedule 여행 스케줄 순서 업데이트
     * case1. 같은 일자 내 스케줄 순서 변경
     * case2. 다른 일자로 스케줄 이동 및 순서 변경
     * @param request TripScheduleOrderUpdateRequest
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/days/{tripDayId}/schedules/reorder")
    public ResponseEntity<ResponseBody> reorderTripSchedule(@PathVariable String tripId, @RequestBody TripScheduleOrderUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.updateTripScheduleOrder(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    /**
     * deleteTripSchedule 여행 스케줄 단건 삭제
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @param tripScheduleId 삭제할 여행 스케줄 ID
     * @return ResponseBody.data : null
     */
    @DeleteMapping("/{tripId}/days/{tripDayId}/schedules/{tripScheduleId}")
    public ResponseEntity<ResponseBody> deleteTripSchedule(
            @PathVariable String tripId, @PathVariable String tripDayId, @PathVariable String tripScheduleId, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.deleteTripSchedule(tripId, tripDayId, tripScheduleId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.DELETE_SUCCESS, null));
    }

    /**
     * addBookmark 북마크 추가
     * case1. DB에 있는 근처 장소이거나 관광지 혹은 맛집의 경우
     * case2. DB에 없는 근처 장소일 경우
     * @param tripId 여행 ID
     * @param request BookmarkCreateRequest
     * @return ResponseBody.data : null
     */
    @PostMapping("/{tripId}/bookmarks")
    public ResponseEntity<ResponseBody> addBookmark(@PathVariable String tripId, @RequestBody BookmarkCreateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        BookmarkResponse data = tripService.createBookmark(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.INSERT_SUCCESS, data));
    }

    /**
     * deleteBookmark 북마크 삭제
     * @param bookmarkId 삭제할 북마크 ID
     * @return ResponseBody.data : null
     */
    @DeleteMapping("/{tripId}/bookmarks/{bookmarkId}")
    public ResponseEntity<ResponseBody> deleteBookmark(@PathVariable String bookmarkId, @AuthenticationPrincipal CustomUserDetails principal) {
        bookmarkService.deleteBookmark(bookmarkId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.DELETE_SUCCESS, null));
    }
}
