package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.HistoryDao;
import com.runhwani.runmate.dto.request.run.RunEndRequest;
import com.runhwani.runmate.model.History;
import com.runhwani.runmate.utils.GpxStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RunServiceImpl implements RunService {

    private final HistoryDao historyDao;

    @Override
    public void endRun(UUID userId, MultipartFile gpxFile, RunEndRequest req) throws IOException {
        // 1. GPX 파일 처리: 유틸 호출

        String gpxFileName = GpxStorageUtil.saveGpxFile(gpxFile);
        log.debug("저장된 GPX 파일명: {}", gpxFileName);

        // 2) History 생성 및 DB 저장
        History history = History.builder()
                .historyId(UUID.randomUUID())
                .userId(userId)
                .gpxFile(gpxFileName)
                .startLocation(req.getStartLocation())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .distance(req.getDistance())
                .avgBpm(req.getAvgBpm())
                .avgPace(req.getAvgPace())
                .avgCadence(req.getAvgCadence())
                .avgElevation(req.getAvgElevation())
                .calories(req.getCalories())
                .createdAt(OffsetDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
        historyDao.insert(history);
    }
}
