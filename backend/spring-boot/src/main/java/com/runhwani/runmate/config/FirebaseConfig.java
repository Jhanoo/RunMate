package com.runhwani.runmate.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // 이미 초기화된 앱이 있는지 확인
            if (FirebaseApp.getApps().isEmpty()) {
                // Firebase Admin SDK 인증 파일 로드
                GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new ClassPathResource("runmate-e11b0-firebase-adminsdk-fbsvc-358767a133.json").getInputStream());
                
                // Firebase 옵션 설정
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                
                // Firebase 앱 초기화
                FirebaseApp.initializeApp(options);
                
                System.out.println("Firebase 애플리케이션이 성공적으로 초기화되었습니다.");
            }
        } catch (IOException e) {
            System.err.println("Firebase 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 