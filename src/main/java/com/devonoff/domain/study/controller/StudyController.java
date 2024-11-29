package com.devonoff.domain.study.controller;

import com.devonoff.domain.study.dto.StudyDto;
import com.devonoff.domain.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class StudyController {

  private final StudyService studyService;

  // 스터디 목록 조회
  @GetMapping
  public ResponseEntity<Page<StudyDto>> getStudyList(Pageable pageable) {
    Page<StudyDto> studyList = studyService.getStudyList(pageable);
    return ResponseEntity.ok(studyList);
  }
}