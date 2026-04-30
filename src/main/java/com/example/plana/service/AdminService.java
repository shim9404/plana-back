package com.example.plana.service;


import com.example.plana.auth.Role;
import com.example.plana.common.exception.BusinessException;
import com.example.plana.common.exception.ErrorCode;
import com.example.plana.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminService {
    private final MemberMapper memberMapper;
    public List<Map<String, Object>> getAllMembers(Map<String, Object> map) {
        List<Map<String, Object>> mList = null;
        mList = memberMapper.memberList(map);
        return mList;
    }

    public void updateMemberRole(String memberId, Role role){

        int result = memberMapper.updateMemberRole(memberId, role.name());
        if (result == 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    public void updateMemberStatus(String memberId, String status){
        int result = memberMapper.updateMemberStatus(memberId, status);
        if (result == 0) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
