package com.example.plana.common.exception;

import com.example.plana.dto.common.ResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;


// 403 에러 예외 처리
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorCode errorCode = ErrorCode.HANDLE_ACCESS_DENIED;

        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json;charset=UTF-8");

        ResponseBody<Object> responseBody = ResponseBody.error(errorCode, errorCode.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}