package com.devonoff.domain.study.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.domain.student.dto.StudentDto;
import com.devonoff.domain.study.dto.StudyDto;
import com.devonoff.domain.study.service.StudyService;
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
  @DisplayName("특정 사용자가 속한 스터디 목록 조회 성공")
  void getStudyList_Success() throws Exception {
    // Given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 12);

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

    when(studyService.getStudyList(eq(userId), any(Pageable.class))).thenReturn(studyDtoPage);

    // When & Then
    mockMvc.perform(get("/api/study/author/{userId}", userId)
            .param("page", "0")
            .param("size", "12"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.content[0].studyName").value("스터디 1"))
        .andExpect(jsonPath("$.content[1].studyName").value("스터디 2"))
        .andExpect(jsonPath("$.totalElements").value(2));

    verify(studyService, times(1)).getStudyList(eq(userId), any(Pageable.class));
  }

  @Test
  @DisplayName("스터디 참가자 목록 조회 성공")
  void getParticipants_Success() throws Exception {
    // Given
    Long studyId = 1L;

    StudentDto student1 = StudentDto.builder()
        .studentId(1L)
        .userId(2L)
        .nickname("참가자1")
        .isLeader(false)
        .build();

    StudentDto student2 = StudentDto.builder()
        .studentId(2L)
        .userId(3L)
        .nickname("참가자2")
        .isLeader(false)
        .build();

    List<StudentDto> participants = List.of(student1, student2);

    when(studyService.getParticipants(anyLong())).thenReturn(participants);

    // When & Then
    mockMvc.perform(get("/api/study/{studyId}/participants", studyId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].nickname").value("참가자1"))
        .andExpect(jsonPath("$[1].nickname").value("참가자2"));

    verify(studyService, times(1)).getParticipants(studyId);
  }
}