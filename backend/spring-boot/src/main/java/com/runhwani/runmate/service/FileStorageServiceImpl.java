package com.runhwani.runmate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일 크기 검증 로그 (디버깅용)
        System.out.println("File size: " + file.getSize() + " bytes");
        System.out.println("Max file size configured: " + maxFileSize);

        // 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // 파일명 충돌 방지를 위한 고유 파일명 생성 (더 짧게)
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        // UUID의 앞부분만 사용하여 파일명 생성 (더 짧게)
        String fileName = UUID.randomUUID().toString().substring(0, 8) + fileExtension;

        // 파일 저장
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 디버깅 로그 추가
        System.out.println("Upload path: " + uploadPath.toString());
        System.out.println("File name: " + fileName);
        System.out.println("Target location: " + targetLocation.toString());
        System.out.println("File URL: " + baseUrl + "/uploads/" + fileName);

        // 파일 접근 URL 생성 (절대 경로로 반환)
        return baseUrl + "/uploads/" + fileName;
    }
} 