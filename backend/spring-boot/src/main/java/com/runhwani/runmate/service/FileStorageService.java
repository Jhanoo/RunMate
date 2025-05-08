package com.runhwani.runmate.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    /**
     * 파일을 저장하고 접근 가능한 URL을 반환
     */
    String storeFile(MultipartFile file) throws IOException;
} 