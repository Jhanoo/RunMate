package com.runhwani.runmate.service;

import com.runhwani.runmate.dao.MarathonDao;
import com.runhwani.runmate.dao.MarathonDistanceDao;
import com.runhwani.runmate.dto.response.marathon.MarathonResponse;
import com.runhwani.runmate.model.Marathon;
import com.runhwani.runmate.model.MarathonDistance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarathonServiceImpl implements MarathonService{

    private final MarathonDao marathonDao;
    private final MarathonDistanceDao distanceDao;

    @Override
    public List<MarathonResponse> getUpcomingMarathons() {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));

        // 1. 마라톤 조회
        List<Marathon> marathons = marathonDao.findUpcomingMarathons(now);

        // 2. 마라톤 거리 조회 & 파싱
        List<MarathonResponse> result = new ArrayList<>();
        for(Marathon m : marathons) {
            List<MarathonDistance> mdList = distanceDao.findByMarathonId(m.getMarathonId());

            List<String> distList = new ArrayList<>();
            for (MarathonDistance md : mdList) {
                distList.add(md.getDistance());
            }

            result.add(MarathonResponse.builder()
                            .marathonId(m.getMarathonId())
                            .name(m.getName())
                            .date(m.getDate().toLocalDate())
                            .location(m.getLocation())
                            .distance(distList)
                            .build()
            );
        }
        return result;
    }
}
