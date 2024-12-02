package com.devonoff.domain.studyTimeline.controller;

import com.devonoff.domain.studyTimeline.dto.StudyTimelineDto;
import com.devonoff.domain.studyTimeline.service.StudyTimelineService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-time")
public class StudyTimelineController {

  private final StudyTimelineService studyTimelineService;

  @GetMapping("/{studyId}")
  public ResponseEntity<List<StudyTimelineDto>> getStudyTimes(@PathVariable Long studyId) {
    return ResponseEntity.ok(this.studyTimelineService.findAllStudyTimelines(studyId));
  }
}
