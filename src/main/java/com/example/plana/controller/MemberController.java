package com.example.plana.controller;

import com.example.plana.auth.CustomUserDetails;
import com.example.plana.common.response.SuccessCode;
import com.example.plana.dto.common.ResponseBody;
import com.example.plana.dto.member.create.MemberCreateRequest;
import com.example.plana.dto.member.read.MemberReadResponse;
import com.example.plana.dto.member.read.MemberTripResponse;
import com.example.plana.dto.member.read.MemberTripTrashResponse;
import com.example.plana.dto.member.update.MemberPwUpdateRequest;
import com.example.plana.dto.member.update.MemberStatusRequest;
import com.example.plana.dto.member.update.MemberUpdateRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.plana.service.MemberService;


import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    /**
     * 이메일을 이용해 회원 가입
     * @param member 회원 가입에 필요한 데이터
     * @return ResponseBody.data : null
     */
    @PostMapping
    public ResponseEntity<ResponseBody> signupByEmail( @RequestBody MemberCreateRequest member ) {

        // 회원 가입 진행
        memberService.createMemberByEmail(member);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.INSERT_SUCCESS, null));
    }

    /** DEV-43
     * dupliNickname(): 닉네임 중복 체크 호출 함수
     *  -> existNickname(): 닉네임 중복 체크(true : 일치(중복) => ErrorCode 호출 / false: 비일치(중복 X) => 새닉네임 반환)
     * @param nickname        // 새 닉네임
     * @return ResponseBody.data : nicknameDupli
     */
    @GetMapping("/nickname/check")
    public ResponseEntity<ResponseBody> dupliNickname(@RequestParam("nickname") String nickname) {
        memberService.existNickname(nickname);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, Map.of("newNickname", nickname)));
    }

    /** DEV-47
     * getMember(): 회원 정보 호출 함수(마이페이지 진입)
     *  -> readMember(): 회원 정보 호출(ID, 이메일, 이름, 닉네임, 프로필 이미지)
     * @param memberId        // 회원 고유 ID
     * @return ResponseBody.data : MemberReadResponse
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<ResponseBody> getMember(@PathVariable("memberId") String memberId, @AuthenticationPrincipal CustomUserDetails principal) {
        MemberReadResponse data = memberService.readMember(principal.getMemberId(), memberId);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, Map.of("member", data)));
    }

    /** DEV-54
     * editMember(): 회원 정보 수정
     *  -> updateMember(): 회원 정보 수정(닉네임, 프로필 이미지)
     * @param memberId              // 회원 고유 ID
     * @param memberUpdateRequest  // 요청 Body - 닉네임, 프로필 이미지
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{memberId}")
    public ResponseEntity<ResponseBody> editMember(@PathVariable("memberId") String memberId, @RequestBody MemberUpdateRequest memberUpdateRequest, @AuthenticationPrincipal CustomUserDetails principal) {
        memberService.updateMember(principal.getMemberId(), memberId, memberUpdateRequest);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    /** DEV-55
     * editPassword(): 회원 비밀번호 수정
     *  -> checkPassword()   : 비밀번호 일치 확인
     *     (true : 일치 => 새 비밀번호 수정 / false: 비일치 => ErrorCode 호출)
     *  -> updatePassword() : 새 비밀번호 변경
     * @param memberId                 // 회원 고유 ID
     * @param memberPwUpdateRequest // 요청 Body - 현재 비밀번호, 새 비밀번호
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{memberId}/password")
    public ResponseEntity<ResponseBody> editPassword(@PathVariable("memberId") String memberId, @RequestBody MemberPwUpdateRequest memberPwUpdateRequest, @AuthenticationPrincipal CustomUserDetails principal) {
        // 비밀번호 갖고오기(current / new)
        String currentPassword = memberPwUpdateRequest.getCurrentPassword();
        String newPassword = memberPwUpdateRequest.getNewPassword();

        // 현재 비밀번호 일치 여부 확인(true : 일치 => 새 비밀번호 수정 / false: 비일치 => ErrorCode 호출)
        memberService.checkPassword(principal.getMemberId(), memberId, currentPassword);

        // 새 비밀번호 변경
        memberService.updatePassword(principal.getMemberId(), memberId, currentPassword, newPassword);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.PASSWORD_UPDATE_SUCCESS, null));
    }

    /** DEV-56
     * withdrawMember(): 회원 탈퇴
     *  -> checkMember()         : 회원 정보 일치 확인
     *     (true : 일치 => 계정 상태 변경(삭제) / false: 비일치 => ErrorCode 호출)
     *  -> updateMemberStatus() : 회원 정보 상태 변경(ACTIVE -> DELETED)
     * @param memberId              // 회원 고유 ID
     * @param memberStatusRequest // 요청 Body - 이메일, 이름, 비밀번호
     * @return ResponseBody.data : null
     */
    @PatchMapping("/{memberId}/withdraw")
    public ResponseEntity<ResponseBody> withdrawMember(@PathVariable("memberId") String memberId, @RequestBody MemberStatusRequest memberStatusRequest, @AuthenticationPrincipal CustomUserDetails principal) {
        // 회원 정보 일치 여부 확인(true : 일치 => 계정 상태 변경(삭제) / false: 비일치 => ErrorCode 호출)
        memberService.checkMember(principal.getMemberId(), memberId, memberStatusRequest);

        // 회원 정보 상태 변경(삭제)
        memberService.updateMemberStatus(principal.getMemberId(), memberId);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.UPDATE_SUCCESS, null));
    }

    /** DEV-57
     * getMyTripList(): 회원 여행 목록 호출(내 여행페이지 진입)
     *  -> readTripByMemberId(): 회원 여행 목록 호출(여행 고유 ID, 여행 이름)
     * @param memberId // 회원 고유 ID
     * @return ResponseBody.data : memberId, List<MemberTripResponse>
     */
    @GetMapping("/{memberId}/trips")
    public ResponseEntity<ResponseBody> getMyTripList(@PathVariable("memberId") String memberId, @AuthenticationPrincipal CustomUserDetails principal) {
        List<MemberTripResponse> data = memberService.readTripByMemberId(principal.getMemberId(), memberId);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, Map.of("member", Map.of("memberId", memberId, "trips", data))));
    }

    /** DEV-119
     * getTripTrash(): INACTIVE(비활성)된 여행 정보 호출(휴지통 진입)
     *  -> readTripTrash(): INACTIVE(비활성)된 여행 정보 호출(여행 고유 ID, 이름, 출발 및 도착 일자, 북마크 및 스케줄 수, 남은 보관일)
     * @param memberId // 회원 고유 ID
     * @return ResponseBody.data : memberId, List<MemberTripTrashResponse>
     */
    @GetMapping("/{memberId}/trips/trashs")
    public ResponseEntity<ResponseBody> getTripTrash(@PathVariable("memberId") String memberId, @AuthenticationPrincipal CustomUserDetails principal) {
        List<MemberTripTrashResponse> data = memberService.readTripTrash(principal.getMemberId(), memberId);

        return ResponseEntity.ok(
                ResponseBody.success(SuccessCode.SELECT_SUCCESS, Map.of("member", Map.of("memberId", memberId, "trips", data))));
    }
}
