package com.example.plana.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
// 서블릿과 컨트롤러의 관계가  필터와 인터셉트 관계와 같다.
@WebFilter(urlPatterns = "/*") //모든 요청에 대해서 RunningTimeFilter적용할 수 있다.
public class RunningTimeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        //1. 전처리
        long startTime = System.currentTimeMillis();//1000이 1초
        //2. 서블릿  또는 필터
        chain.doFilter(request, response);

        //3. 후처리
        long endTime = System.currentTimeMillis();
        System.out.print("[[ "+((HttpServletRequest)request).getRequestURI()+" ]]");
        System.out.println("time : "+(endTime - startTime));
    }
}
