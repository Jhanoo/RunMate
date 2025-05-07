// src/main/java/com/runhwani/runmate/config/WebLoggingConfig.java
package com.runhwani.runmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class WebLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);      // 클라이언트 IP, 세션ID 등
        filter.setIncludeQueryString(true);     // 쿼리스트링
        filter.setIncludePayload(true);         // 요청 바디(JSON 등)
        filter.setMaxPayloadLength(10000);      // 최대 바디 길이
        filter.setIncludeHeaders(false);        // 헤더도 보고 싶으면 true
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }
}
