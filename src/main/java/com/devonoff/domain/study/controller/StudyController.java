package com.devonoff.domain.study.controller;

import com.devonoff.domain.student.dto.StudentDto;
import com.devonoff.domain.study.dto.StudyDto;
import com.devonoff.domain.study.service.StudyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  // 특정 사용자가 속한 스터디 목록 조회
  @GetMapping("/author/{userId}")
  public ResponseEntity<Page<StudyDto>> getStudyList(
      @PathVariable Long userId,
      Pageable pageable) {
    Page<StudyDto> studyList = studyService.getStudyList(userId, pageable);
    return ResponseEntity.ok(studyList);
  }

  // 스터디 참가자 목록 조회
  @GetMapping("/{studyId}/participants")
  public ResponseEntity<List<StudentDto>> getParticipants(@PathVariable Long studyId) {
    List<StudentDto> participants = studyService.getParticipants(studyId);
    return ResponseEntity.ok(participants);
  }
}