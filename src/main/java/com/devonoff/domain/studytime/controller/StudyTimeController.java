package com.devonoff.domain.studytime.controller;

import com.devonoff.domain.studytime.dto.StudyTimeDto;
import com.devonoff.domain.studytime.service.StudyTimeService;
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
public class StudyTimeController {

  private final StudyTimeService studyTimeService;

  @GetMapping("/{studyId}")
  public ResponseEntity<List<StudyTimeDto>> getStudyTimes(@PathVariable Long studyId) {
    return ResponseEntity.ok(this.studyTimeService.findAllStudyTimes(studyId));
  }
}
