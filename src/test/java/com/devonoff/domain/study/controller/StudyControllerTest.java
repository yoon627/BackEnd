package com.devonoff.domain.study.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.domain.study.dto.StudyDto;
import com.devonoff.domain.study.service.StudyService;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.type.StudyStatus;
import com.devonoff.util.JwtProvider;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudyController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
class StudyControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private StudyService studyService;

  @MockBean
  private JwtProvider jwtProvider;

  @Test
  @DisplayName("본인이 속한 스터디 목록 조회 성공")
  void getStudyList_Success() throws Exception {
    // Given
    Pageable pageable = PageRequest.of(0, 20);

    StudyDto studyDto1 = StudyDto.builder()
        .id(1L)
        .studyName("스터디 1")
        .status(StudyStatus.PENDING)
        .build();

    StudyDto studyDto2 = StudyDto.builder()
        .id(2L)
        .studyName("스터디 2")
        .status(StudyStatus.IN_PROGRESS)
        .build();

    List<StudyDto> studyDtos = List.of(studyDto1, studyDto2);
    Page<StudyDto> studyDtoPage = new PageImpl<>(studyDtos, pageable, studyDtos.size());

    when(studyService.getStudyList(any(Pageable.class))).thenReturn(studyDtoPage);

    // When & Then
    mockMvc.perform(get("/api/study")
            .param("page", "0")
            .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[0].studyName").value("스터디 1"))
        .andExpect(jsonPath("$.content[1].studyName").value("스터디 2"))
        .andExpect(jsonPath("$.totalElements").value(2));

    verify(studyService, times(1)).getStudyList(any(Pageable.class));
  }
}