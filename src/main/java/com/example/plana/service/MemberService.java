package com.example.plana.service;

import com.example.plana.auth.Role;
import com.example.plana.auth.SocialType;
import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.dto.member.create.MemberCreateRequest;
import com.example.plana.dto.member.read.MemberReadResponse;
import com.example.plana.dto.member.read.MemberTripResponse;
import com.example.plana.dto.member.read.MemberTripTrashResponse;
import com.example.plana.dto.member.update.MemberStatusRequest;
import com.example.plana.dto.member.update.MemberUpdateRequest;
import com.example.plana.mapper.MemberMapper;
import com.example.plana.model.Member;
import com.example.plana.model.MemberSave;
import com.example.plana.model.MemberVerify;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberMapper memberMapper;

    @Value("${app.upload-path}")
    private String uploadPath;


    /**
     * 권한 체크
     * 데이터 CRUD를 요청한 id와 현재 로그인한 id를 비교하는 함수
     * @param tokenMemberId 토큰에서 추출한 사용자 id
     * @param pathMemberId url에서 추출한 사용자 id
     */
    public void validateOwner(String tokenMemberId, String pathMemberId, Role role) {
        log.info("pathId:: ",pathMemberId);
        log.info("tokenMemberId:: ",tokenMemberId);
        log.info("접근 권한 체크 발생");
        if (role == Role.ADMIN) return;;

        if (!tokenMemberId.equals(pathMemberId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    // 닉네임 중복 체크
    public void existNickname(String nickname) {
        if (memberMapper.existNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    // 회원 정보 호출
    public MemberReadResponse readMember(String tokenMemberId, String pathMemberId, Role role) {
        validateOwner(tokenMemberId, pathMemberId, role);

        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (memberMapper.readMember(tokenMemberId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 회원 정보 호출
        return memberMapper.readMember(tokenMemberId);
    }

    // 회원 정보 호출
    public Member readMemberByEmail(String email) {
        Member member = memberMapper.readMemberByEmail(email);
        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (member == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 회원 정보 호출
        return member;
    }

    // 회원 정보 호출
    public Member readMemberById(String memberId) {

        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (memberMapper.readMember(memberId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 회원 정보 호출
        return memberMapper.readMemberById(memberId);
    }

    // 회원 정보 수정
    public void updateMember(String tokenMemberId, String pathMemberId, Role role, MemberUpdateRequest memberUpdateRequest) {
        validateOwner(tokenMemberId, pathMemberId, role);

        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (memberMapper.readMember(tokenMemberId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 관리자가 다른 사용자의 데이터를 업데이트할 때 path로 보낸걸로 판단하게 해야함. 아니면 관리자꺼 업데이트됨 
        memberMapper.updateMember(pathMemberId, memberUpdateRequest);
    }

    // 현재 비밀번호 일치 여부 확인
    public void checkPassword(String tokenMemberId, String pathMemberId, Role role, String currentPassword) {
        validateOwner(tokenMemberId, pathMemberId, role);

        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (memberMapper.readMember(tokenMemberId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // DB에서 암호화된 비밀번호 가져오기
        String encodedPassword = memberMapper.checkPassword(tokenMemberId);

        // 비밀번호 일치 여부 확인(true: 일치 / false: 비일치)
        //  - 같은 비밀번호여도 암호화할 때마다 다른 암호화 결과를 생성됨
        //   -> 내부적으로 salt, 해시값 비교
        boolean check  = bCryptPasswordEncoder.matches(currentPassword, encodedPassword);

        if (!check) { throw new BusinessException(ErrorCode.PASSWORD_MISMATCH); }
    }

    // 새 비밀번호 변경
    public void updatePassword(String tokenMemberId, String pathMemberId, Role role, String currentPassword, String newPassword) {
        validateOwner(tokenMemberId, pathMemberId, role);

        // 현재 비밀번호와 새 비밀번호가 일치 여부 확인(true : 일치 => ErrorCode 호출)
        boolean check = currentPassword.equals(newPassword);
        if (check) { throw new BusinessException(ErrorCode.PASSWORD_DUPLICATE); }

        // 새 비밀번호 암호화
        newPassword = bCryptPasswordEncoder.encode(newPassword);

        // 새 비밀번호로 변경
        memberMapper.updatePassword(tokenMemberId, newPassword);
    }

    // 회원 정보 일치 여부 확인
    public void checkMember(String tokenMemberId, String pathMemberId, Role role, MemberStatusRequest memberStatusRequest) {
        validateOwner(tokenMemberId, pathMemberId, role);

        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (memberMapper.readMember(tokenMemberId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        MemberVerify memberVerify = memberMapper.checkMember(tokenMemberId);
        // 이메일, 이름, 비밀번호 일치 확인
        if (!memberVerify.getEmail().equals(memberStatusRequest.getEmail()) ||
                !memberVerify.getName().equals(memberStatusRequest.getName()) ||
                !bCryptPasswordEncoder.matches(memberStatusRequest.getPassword(), memberVerify.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_MISMATCH);
        }
    }

    //  회원 정보 상태 변경(삭제)
    public void updateMemberStatus(String tokenMemberId, String pathMemberId, Role role) {
        memberMapper.updateMemberStatus(tokenMemberId, "DELETED");
    }

    // 회원 정보 삭제(자동 실행)
    public void deleteOldMembers() {
        memberMapper.deleteOldMembers();
    }

    // 회원 여행 목록 호출
    public List<MemberTripResponse> readTripByMemberId(String tokenMemberId, String pathMemberId, Role role) {
        validateOwner(tokenMemberId, pathMemberId, role);

        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (memberMapper.readMember(tokenMemberId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return memberMapper.readTripByMemberId(tokenMemberId);
    }

    // 회원가입 : email로 가입했을 경우 호출
    @Transactional(rollbackFor = Exception.class)
    public void createMemberByEmail(MemberCreateRequest member) {

        MemberSave memberSave = new MemberSave();

        memberSave.setName(member.getName());
        memberSave.setEmail(member.getEmail());
        memberSave.setNickname(member.getNickname());
        memberSave.setRole(Role.MEMBER.name());
        memberSave.setSocialType(SocialType.EMAIL.name());
        memberSave.setPassword(bCryptPasswordEncoder.encode(member.getPassword()));

        int result = memberMapper.createMember(memberSave);

        // 회원가입 실패
        if (result != 1){
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }
    }

    
    // 프로필 이미지 저장
    private String storeProfileImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            // 이미지 파일만 업로드 가능합니다.
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        // 파일 크기 5MB
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PAYLOAD_TOO_LARGE);
        }
        Path dir = Paths.get(uploadPath.trim());
        Files.createDirectories(dir);

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
            if (ext.length() > 12) {
                ext = "";
            }
        }
        String storedName = UUID.randomUUID() + ext;

        Path target = dir.resolve(storedName).normalize();
        if (!target.startsWith(dir.normalize())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_PATH);
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    // INACTIVE(비활성)된 여행 정보 호출
    public List<MemberTripTrashResponse> readTripTrash(String tokenMemberId, String pathMemberId, Role role) {
        validateOwner(tokenMemberId, pathMemberId, role);

        // 회원 정보 존재 하지 않을 시, ErrorCode 호출
        if (memberMapper.readMember(tokenMemberId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return  memberMapper.readTripTrash(tokenMemberId);
    }

    // 여행 정보 삭제(자동 실행)
    public void deleteOldTrips() {
        memberMapper.deleteOldTrips();
    }
}
