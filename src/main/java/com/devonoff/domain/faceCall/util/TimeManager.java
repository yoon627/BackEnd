package com.devonoff.domain.faceCall.util;

import com.devonoff.domain.studyTimeline.service.StudyTimelineService;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeManager {

  private final ConcurrentHashMap<String, LocalDateTime> timer = new ConcurrentHashMap<>();
  private final StudyTimelineService studyTimelineService;

  public void startTimer(String studyId) {
    timer.put(studyId, LocalDateTime.now());
  }

  public void endTimer(String studyId) {
    studyTimelineService.saveStudyTimeline(Long.valueOf(studyId), timer.get(studyId),
        LocalDateTime.now());
    timer.remove(studyId);
  }

}
