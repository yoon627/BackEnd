package com.devonoff.domain.studySignup.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.domain.studySignup.dto.StudySignupCreateRequest;
import com.devonoff.domain.studySignup.dto.StudySignupDto;
import com.devonoff.domain.studySignup.service.StudySignupService;
import com.devonoff.type.StudySignupStatus;
import com.devonoff.util.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudySignupController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
class StudySignupControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private StudySignupService studySignupService;

  @MockBean
  private JwtProvider jwtProvider;

  @Test
  @DisplayName("스터디 신청 성공")
  void createStudySignup_Success() throws Exception {
    // Given
    StudySignupDto response = StudySignupDto.builder()
        .signupId(10L)
        .userId(1L)
        .nickName("참가자")
        .status(StudySignupStatus.PENDING)
        .build();

    when(studySignupService.createStudySignup(any(StudySignupCreateRequest.class)))
        .thenReturn(response);

    // When & Then
    mockMvc.perform(post("/api/study-signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"studyPostId\": 100, \"userId\": 1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.signupId").value(10L))
        .andExpect(jsonPath("$.userId").value(1L))
        .andExpect(jsonPath("$.nickName").value("참가자"))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(studySignupService, times(1)).createStudySignup(any(StudySignupCreateRequest.class));
  }
}
