package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.CurriculumDao;
import com.runhwani.runmate.dao.HistoryDao;
import com.runhwani.runmate.dao.UserDao;
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
    private final UserDao userDao;

    @Override
    public UUID endRun(UUID userId, MultipartFile gpxFile, RunEndRequest req) throws IOException {
        // 1. GPX 파일 처리: 유틸 호출

        String gpxFileName = GpxStorageUtil.saveGpxFile(gpxFile);
        log.debug("저장된 GPX 파일명: {}", gpxFileName);

        // 2) History 생성 및 DB 저장
        History history = History.builder()
                .historyId(UUID.randomUUID())
                .userId(userId)
                .groupId(req.getGroupId())
                .courseId(req.getCourseId())
                .gpxFile("https://k12d107.p.ssafy.io/gpx/" + gpxFileName)
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

        // users : avgPace 업데이트
        // 3-1) 현재 저장된 historyId 조회
        long count = historyDao.countByUserId(userId);
        // 3-2) 이전에 users table에 저장된 avg_pace 조회
        Double prevAvg = userDao.findAvgPaceByUserId(userId);
        // 3-3) 평균 페이스 새롭게 계산
        double updatedAvgPace = (
                ((prevAvg != null ? prevAvg : 0.0) * (count - 1)) + req.getAvgPace()
        ) / count;
        // 3-4) users table update
        userDao.updateAvgPace(userId, updatedAvgPace);
        log.debug("User({}) avg_pace updated to {}", userId, updatedAvgPace);

        Todo todo = curriculumDao.selectTodayTodoByUserId(userId);

        if (todo != null && todo.getIsDone() != null && !todo.getIsDone()) {
            String content = todo.getContent();

            double requiredDistKm = 0.0;

            if (content.startsWith("인터벌:")) {
                Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)km\\s*[×x]\\s*(\\d+)회");
                Matcher m = p.matcher(content);
                if (m.find()) {
                    double distanceKm = Double.parseDouble(m.group(1));
                    int repeatCount = Integer.parseInt(m.group(2));
                    requiredDistKm = distanceKm * repeatCount;
                }
            } else if (content.startsWith("템포주:")) {
                // 템포런 구간 거리 합산
                Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)km");
                Matcher m = p.matcher(content);
                while (m.find()) {
                    requiredDistKm += Double.parseDouble(m.group(1));
                }
            } else {
                // 일반 km 기반 훈련 (LSD, 회복주 등)
                Pattern p = Pattern.compile("^[^:]+:\\s*(\\d+(?:\\.\\d+)?)km");
                Matcher m = p.matcher(content);
                if (m.find()) {
                    requiredDistKm = Double.parseDouble(m.group(1));
                }
            }

            if (requiredDistKm > 0 && requiredDistKm * 0.95 <= history.getDistance()) {
                curriculumDao.updateTodoDone(todo.getTodoId());
            }
        }
        
        return history.getHistoryId();
    }
}
