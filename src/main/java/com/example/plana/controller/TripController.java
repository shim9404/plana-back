package com.example.plana.controller;

import com.example.plana.auth.CustomUserDetails;
import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.bookmark.create.BookmarkCreateRequest;
import com.example.plana.dto.bookmark.read.BookmarkResponse;
import com.example.plana.dto.common.EmptyData;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.common.StatusUpdateRequest;
import com.example.plana.dto.trip.create.TripCreateRequest;
import com.example.plana.dto.trip.create.TripCreateResponse;
import com.example.plana.dto.trip.create.TripScheduleCreateResponse;
import com.example.plana.dto.trip.read.TripResponse;
import com.example.plana.dto.trip.update.*;
import com.example.plana.service.BookmarkService;
import com.example.plana.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Tag(name = "Trip API", description = "여행 API 목록")
public class TripController {

    private final TripService tripService;
    private final BookmarkService bookmarkService;

    /**
     * generateTrip 여행 신규 생성 호출 함수
     * @param request TripRequest
     * @return ResponseBody.data : TripResponse
     */
    @PostMapping
    @Operation(summary = "새 여행 생성", description = "신규 여행 계획을 생성한다.")
    @ApiResponse(responseCode = "201", description = "[S002] 등록이 완료되었습니다.")
    public ResponseEntity<ResponseBody<TripCreateResponse>> generateTrip(@RequestBody TripCreateRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails principal) {
        TripCreateResponse data = tripService.createTrip(request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.INSERT_SUCCESS, data));
    }

    /**
     * getTrip 여행 정보 조회(단건/상세)
     * @param tripId 여행 ID
     * @return ResponseBody.data : TripResponse
     */
    @GetMapping("/{tripId}")
    @Operation(summary = "단일 여행 정보 호출", description = "내 여행의 상세 정보를 요청한다.")
    @Parameters({ @Parameter(name = "tripId", description = "여행 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S001] 조회에 성공하였습니다.")
    public ResponseEntity<ResponseBody<TripResponse>> getTrip(@PathVariable String tripId, @AuthenticationPrincipal CustomUserDetails principal) {
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
    @Operation(summary = "[변경 예정] 여행 전체 저장", description = "작성 중인 여행 전체를 저장한다.")
    @Parameters({ @Parameter(name = "tripId", description = "여행 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<TripUpdateResponse>> saveTrip(@PathVariable String tripId, @RequestBody TripUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        TripUpdateResponse data = tripService.saveTrip(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
    }

    /**
     * deleteTrip 여행 단건 삭제 - 하위 일자 및 스케줄 전체 삭제
     * @param tripId 여행 ID
     * @return ResponseBody.data : null
     */
    @DeleteMapping("/{tripId}")
    @Operation(summary = "여행 영구 삭제", description = "휴지통에 보관 중인 여행을 영구 삭제한다.")
    @Parameters({ @Parameter(name = "tripId", description = "여행 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S004] 삭제가 완료되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> deleteTrip(@PathVariable String tripId, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.deleteTrip(tripId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.DELETE_SUCCESS));
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
    @Operation(summary = "북마크 등록", description = "북마크한 장소 정보들을 저장한다. 또한, AREA DB에서 장소 정보(근처 장소)가 없는 경우에도 추가 저장한다.")
    @Parameters({ @Parameter(name = "tripId", description = "여행 ID", required = true) })
    @ApiResponse(responseCode = "201", description = "[S002] 등록이 완료되었습니다.")
    public ResponseEntity<ResponseBody<BookmarkResponse>> addBookmark(@PathVariable String tripId, @RequestBody BookmarkCreateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        BookmarkResponse data = tripService.createBookmark(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.INSERT_SUCCESS, data));
    }

    /**
     * deleteBookmark 북마크 삭제
     * @param bookmarkId 삭제할 북마크 ID
     * @return ResponseBody.data : null
     */
    @DeleteMapping("/{tripId}/bookmarks/{bookmarkId}")
    @Operation(summary = "북마크 삭제", description = "등록된 북마크를 목록에서 삭제한다.")
    @Parameters({
            @Parameter(name = "tripId", description = "여행 ID", required = true),
            @Parameter(name = "bookmarkId", description = "삭제할 북마크 ID", required = true),
    })
    @ApiResponse(responseCode = "200", description = "[S004] 삭제가 완료되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> deleteBookmark(@PathVariable String bookmarkId, @AuthenticationPrincipal CustomUserDetails principal) {
        bookmarkService.deleteBookmark(bookmarkId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.DELETE_SUCCESS));
    }

    /**
     * editTripDate 여행 일자(시작 일자, 종료 일자) 갱신 후 부족한 일자 생성 및 반환
     * @param tripId 여행 ID
     * @param request TripDateUpdateRequest
     * @return ResponseBody.data : TripDate
     * UpdateResponse
     */
    @PatchMapping("/{tripId}/date")
    @Operation(summary = "여행 날짜 재선택", description = "여행의 시작일과 종료일을 선택한 날짜로 갱신한다.")
    @Parameters({ @Parameter(name = "tripId", description = "여행 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<TripDateUpdateResponse>> editTripDate(@PathVariable String tripId, @RequestBody TripDateUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        TripDateUpdateResponse data = tripService.updateTripDate(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS, data));
    }

    /**
     * editTripInfo 여행 정보(여행명, 참여 인원) 갱신
     * @param tripId 여행 ID
     * @param request TripInfoUpdateRequest
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/info")
    @Operation(summary = "여행 정보 변경", description = "여행의 이름과 참여 인원을 갱신한다.")
    @Parameters({ @Parameter(name = "tripId", description = "여행 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> editTripInfo(@PathVariable String tripId, @RequestBody TripInfoUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.updateTripInfo(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS));
    }

    /**
     * editTripStatus 여행 상태(ACTIVE/INACTIVE/DELETED) 갱신
     * @param tripId 여행 ID
     * @param request TripStatusUpdateResponse
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/status")
    @Operation(summary = "여행 상태(STATUS) 변경", description = "여행을 휴지통에 버리거나 휴지통에서 복구한다.")
    @Parameters({ @Parameter(name = "tripId", description = "여행 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> editTripStatus(@PathVariable String tripId, @RequestBody StatusUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.updateTripStatus(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS));
    }

    /**
     * deleteTripDay 여행 일자 삭제(여행 일자 하위 스케줄 포함)
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @return ResponseBody.data : null
     */
    @DeleteMapping("/{tripId}/days/{tripDayId}")
    @Operation(summary = "여행 일자 삭제", description = "여행 일정과 일치하지 않는 여행 일자를 최종 확인 후 영구 삭제한다.")
    @Parameters({
            @Parameter(name = "tripId", description = "여행 ID", required = true),
            @Parameter(name = "tripDayId", description = "삭제할 일자 ID", required = true),
    })
    @ApiResponse(responseCode = "200", description = "[S004] 삭제가 완료되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> deleteTripDay(@PathVariable String tripId, @PathVariable String tripDayId, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.deleteTripDay(tripId, tripDayId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.DELETE_SUCCESS));
    }

    /**
     * reorderTripDays 여행 일자 재정렬(전체 일괄)
     * @param tripId 여행 ID
     * @param request TripDayOrderUpdateRequest
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/days/reorder")
    @Operation(summary = "여행 일자 순서 변경", description = "여행에 속한 여행 일자들의 정렬 순서를 갱신한다.")
    @Parameters({ @Parameter(name = "tripId", description = "여행 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> reorderTripDays(@PathVariable String tripId, @RequestBody TripDayOrderUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.updateTripDaysIndexSort(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS));
    }

    /**
     * addTripSchedule 여행 스케줄 신규 추가
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @return ResponseBody.data : TripScheduleCreateResponse
     */
    @PostMapping("/{tripId}/days/{tripDayId}/schedules")
    @Operation(summary = "여행 스케줄 추가", description = "현재 편집 중인 일차의 마지막에 여행 스케줄을 한 행 추가한다.")
    @Parameters({
            @Parameter(name = "tripId", description = "여행 ID", required = true),
            @Parameter(name = "tripDayId", description = "스케줄을 추가할 일자 ID", required = true),
    })
    @ApiResponse(responseCode = "201", description = "[S002] 등록이 완료되었습니다.")
    public ResponseEntity<ResponseBody<TripScheduleCreateResponse>> addTripSchedule(
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
    @Operation(summary = "여행 스케줄 수정", description = "여행 스케줄에 작성 혹은 수정된 내용을 갱신한다.")
    @Parameters({
            @Parameter(name = "tripId", description = "여행 ID", required = true),
            @Parameter(name = "tripDayId", description = "스케줄이 속한 일자 ID", required = true),
            @Parameter(name = "tripScheduleId", description = "수정할 스케줄 ID", required = true),
    })
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> editTripSchedule(
            @PathVariable String tripId, @PathVariable String tripScheduleId, @RequestBody TripScheduleUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.updateTripSchedule(tripId, tripScheduleId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS));
    }

    /**
     * reorderTripSchedule 여행 스케줄 순서 업데이트
     * case1. 같은 일자 내 스케줄 순서 변경
     * case2. 다른 일자로 스케줄 이동 및 순서 변경
     * @param request TripScheduleOrderUpdateRequest
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{tripId}/days/schedules/reorder")
    @Operation(summary = "여행 스케줄 순서 변경", description = "여행에 속한 여행 스케줄들의 정렬 순서 및 소속 일차를 갱신한다.")
    @Parameters({ @Parameter(name = "tripId", description = "여행 ID", required = true) })
    @ApiResponse(responseCode = "200", description = "[S003] 수정이 정상적으로 처리되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> reorderTripSchedule(@PathVariable String tripId, @RequestBody TripScheduleOrderUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.updateTripScheduleOrder(tripId, principal.getMemberId(), request);
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.UPDATE_SUCCESS));
    }

    /**
     * deleteTripSchedule 여행 스케줄 단건 삭제
     * @param tripId 여행 ID
     * @param tripDayId 여행 일자 ID
     * @param tripScheduleId 삭제할 여행 스케줄 ID
     * @return ResponseBody.data : null
     */
    @DeleteMapping("/{tripId}/days/{tripDayId}/schedules/{tripScheduleId}")
    @Operation(summary = "여행 영구 삭제", description = "휴지통에 보관 중인 여행을 영구 삭제한다.")
    @Parameters({
            @Parameter(name = "tripId", description = "여행 ID", required = true),
            @Parameter(name = "tripDayId", description = "스케줄이 속한 일자 ID", required = true),
            @Parameter(name = "tripScheduleId", description = "삭제할 스케줄 ID", required = true),
    })
    @ApiResponse(responseCode = "200", description = "[S004] 삭제가 완료되었습니다.")
    public ResponseEntity<ResponseBody<EmptyData>> deleteTripSchedule(
            @PathVariable String tripId, @PathVariable String tripDayId, @PathVariable String tripScheduleId, @AuthenticationPrincipal CustomUserDetails principal) {
        tripService.deleteTripSchedule(tripId, tripDayId, tripScheduleId, principal.getMemberId());
        return ResponseEntity.ok(ResponseBody.success(SuccessCode.DELETE_SUCCESS));
    }

}
