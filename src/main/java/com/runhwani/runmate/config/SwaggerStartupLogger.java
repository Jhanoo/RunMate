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

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
        String url = String.format("http://localhost:%d/swagger-ui/index.html", port);
        log.info("▶︎ Swagger UI available at: {}", url);
    }
}
