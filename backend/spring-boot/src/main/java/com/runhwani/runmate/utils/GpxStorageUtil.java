package com.runhwani.runmate.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * GPX 파일 저장 관련 공통 유틸리티
 */
@Slf4j
public class GpxStorageUtil {

    private static final String gpxStoragePath = "./gpx";

    /**
     * GPX 파일을 지정된 디렉토리에 저장하고, 저장된 파일명을 반환한다.
     *
     * @param gpxFile        업로드된 GPX MultipartFile
     * @return 저장된 GPX 파일명 (예: "uuid.gpx")
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    public static String saveGpxFile(MultipartFile gpxFile) throws IOException {
        if (gpxFile == null || gpxFile.isEmpty()) {
            return null;
        }

        // 1) 저장할 파일명 생성
        String fileName = UUID.randomUUID() + ".gpx";

        log.debug("gpxStoragePath: {}", gpxStoragePath);
        
        // 2) 디렉토리 존재 확인 및 생성
        Path dirPath = Paths.get(gpxStoragePath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        log.debug("gpx 디렉토리 확인");
        
        // 3) 파일 복사 (덮어쓰기 옵션)
        Path targetPath = dirPath.resolve(fileName);
        Files.copy(gpxFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.debug("파일 생성 완료");
        return fileName;
    }

    /**
     * 저장된 GPX 파일을 삭제한다.
     *
     * @param gpxFileName 삭제할 GPX 파일명
     * @throws IOException 삭제 중 오류 발생 시
     */
    public static void deleteGpxFile(String gpxFileName) throws IOException {
        if (gpxFileName == null || gpxFileName.isBlank()) {
            return;
        }
        Path path = Paths.get(gpxStoragePath).resolve(gpxFileName);
        log.debug("gpx 파일 삭제 시도 : {}", path);
        Files.deleteIfExists(path);
    }
}