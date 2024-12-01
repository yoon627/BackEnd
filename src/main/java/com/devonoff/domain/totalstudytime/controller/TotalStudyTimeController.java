package com.devonoff.domain.totalstudytime.controller;

import com.devonoff.domain.totalstudytime.dto.TotalStudyTimeDto;
import com.devonoff.domain.totalstudytime.service.TotalStudyTimeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/total-study-time")
public class TotalStudyTimeController {

  private final TotalStudyTimeService totalStudyTimeService;

  @GetMapping("/{studyId}")
  public ResponseEntity<TotalStudyTimeDto> getTotalStudyTime(@PathVariable Long studyId) {
    return ResponseEntity.ok(
        this.totalStudyTimeService.getTotalStudyTime(studyId));
  }

  @GetMapping("/ranking")
  public ResponseEntity<List<TotalStudyTimeDto>> getTotalStudyTimeRanking() {
    return ResponseEntity.ok(
        this.totalStudyTimeService.getTotalStudyTimeRanking());
  }
}
