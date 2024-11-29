package com.devonoff.domain.studySignup.controller;

import com.devonoff.domain.studySignup.dto.StudySignupCreateDto;
import com.devonoff.domain.studySignup.dto.StudySignupCreateDto.Response;
import com.devonoff.domain.studySignup.service.StudySignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-signup")
public class StudySignupController {

  private final StudySignupService studySignupService;

  @PostMapping
  public ResponseEntity<Response> createStudySignup(
      @RequestBody @Valid StudySignupCreateDto.Request request) {
    StudySignupCreateDto.Response response = studySignupService.createStudySignup(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}