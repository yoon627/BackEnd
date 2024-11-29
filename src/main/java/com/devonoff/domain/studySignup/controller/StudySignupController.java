package com.devonoff.domain.studySignup.controller;

import com.devonoff.domain.studySignup.dto.StudySignupCreateDto;
import com.devonoff.domain.studySignup.dto.StudySignupCreateDto.Response;
import com.devonoff.domain.studySignup.service.StudySignupService;
import com.devonoff.type.StudySignupStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-signup")
public class StudySignupController {

  private final StudySignupService studySignupService;

  // 스터디 신청
  @PostMapping
  public ResponseEntity<Response> createStudySignup(
      @RequestBody @Valid StudySignupCreateDto.Request request) {
    StudySignupCreateDto.Response response = studySignupService.createStudySignup(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 신청 상태 관리(승인/거절)
  @PatchMapping("/{signupId}")
  public ResponseEntity<Void> updateSignupStatus(
      @PathVariable Long signupId, @RequestParam StudySignupStatus newStatus) {
    studySignupService.updateSignupStatus(signupId, newStatus);
    return ResponseEntity.noContent().build();
  }
}