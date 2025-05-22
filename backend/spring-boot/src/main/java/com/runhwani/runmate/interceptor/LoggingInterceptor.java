package com.runhwani.runmate.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("[REQUEST] {} {} (Client IP: {})",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());

        // 요청 파라미터 로깅
        if (!request.getParameterMap().isEmpty()) {
            log.info("[REQUEST PARAMS] {}", request.getParameterMap());
        }

        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();

        log.info("[RESPONSE] {} {} (Status: {}) - {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                endTime - startTime);
    }
}