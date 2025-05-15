package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.CurriculumDao;
import com.runhwani.runmate.dao.HistoryDao;
import com.runhwani.runmate.dto.request.run.RunEndRequest;
import com.runhwani.runmate.model.History;
import com.runhwani.runmate.model.Todo;
import com.runhwani.runmate.utils.GpxStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class RunServiceImpl implements RunService {

    private final HistoryDao historyDao;
    private final CurriculumDao curriculumDao;

    @Override
    public void endRun(UUID userId, MultipartFile gpxFile, RunEndRequest req) throws IOException {
        // 1. GPX 파일 처리: 유틸 호출

        String gpxFileName = GpxStorageUtil.saveGpxFile(gpxFile);
        log.debug("저장된 GPX 파일명: {}", gpxFileName);

        // 2) History 생성 및 DB 저장
        History history = History.builder()
                .historyId(UUID.randomUUID())
                .userId(userId)
                .groupId(req.getGroupId())
                .courseId(req.getCourseId())
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

        Todo todo = curriculumDao.selectTodayTodoByUserId(userId);
        if (todo != null) {
            Pattern p = Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)km.*");
            Matcher m = p.matcher(todo.getContent());

            if (m.find()) {
                double todoDist = Double.parseDouble(m.group(1));

                if (todoDist * 0.95 <= history.getDistance()) {
                    curriculumDao.updateTodoDone(todo.getTodoId());
                }
            }
        }
    }
}
