package com.runhwani.runmate.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
@Slf4j
public class SwaggerStartupLogger implements ApplicationListener<WebServerInitializedEvent> {

    private int port;

    /**
     * application.yml 또는 properties 에서 swagger-ui.path 를 다르게 설정하셨다면
     * 아래 값을 그에 맞춰 변경해주세요.
     * (기본값: /swagger-ui/index.html)
     */
    @Value("${springdoc.swagger-ui.path:/swagger-ui/index.html}")
    private String swaggerPath;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
        String url = String.format("http://localhost:%d%s", port, swaggerPath);
        log.info("▶︎ Swagger UI available at: {}", url);
    }
}
