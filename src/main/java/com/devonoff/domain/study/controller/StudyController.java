package com.devonoff.domain.study.controller;

import com.devonoff.domain.study.service.StudyService;
import com.devonoff.domain.study.dto.StudyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class StudyController {

  private final StudyService studyService;

  // 스터디 상세 조회
  @GetMapping("/{studyId}")
  public ResponseEntity<StudyDto> getStudyDetail(@PathVariable Long studyId) {
    StudyDto studyDto = studyService.getStudyDetail(studyId);
    return ResponseEntity.ok(studyDto);
  }
}