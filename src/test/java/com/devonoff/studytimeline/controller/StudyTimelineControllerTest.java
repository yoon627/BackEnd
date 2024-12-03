package com.devonoff.studytimeline.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.config.SecurityConfig;
import com.devonoff.domain.studyTimeline.controller.StudyTimelineController;
import com.devonoff.domain.studyTimeline.dto.StudyTimelineDto;
import com.devonoff.domain.studyTimeline.service.StudyTimelineService;
import com.devonoff.util.JwtProvider;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudyTimelineController.class)
@Import(SecurityConfig.class) // SecurityConfig를 명시적으로 포함 (Optional)
@AutoConfigureMockMvc(addFilters = false)
class StudyTimelineControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private StudyTimelineService studyTimelineService;

  @MockBean
  private JwtProvider jwtProvider;

  @Test
  @DisplayName("학습했던 전체 시간대 조회 - 성공")
  void testFindAllStudyTimelinesForGivenStudyId_success() throws Exception {
    // Given
    Long studyId = 1L;
    List<StudyTimelineDto> mockStudyTimes = Arrays.asList(
        StudyTimelineDto.builder()
            .studyId(1L)
            .studyName("Java Study Group")
            .startedAt(LocalDateTime.of(2023, 12, 1, 10, 0))
            .endedAt(LocalDateTime.of(2023, 12, 1, 12, 0))
            .build(),
        StudyTimelineDto.builder()
            .studyId(2L)
            .studyName("Java Study Group")
            .startedAt(LocalDateTime.of(2023, 12, 2, 9, 0))
            .endedAt(LocalDateTime.of(2023, 12, 2, 11, 0))
            .build()
    );
    given(studyTimelineService.findAllStudyTimelines(studyId)).willReturn(mockStudyTimes);

    // When
    mockMvc.perform(get("/api/study-time/{studyId}", studyId)
            .contentType(MediaType.APPLICATION_JSON))
        // Then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()").value(2))
        .andExpect(jsonPath("$[0].studyId").value(1L))
        .andExpect(jsonPath("$[0].studyName").value("Java Study Group"))
        .andExpect(jsonPath("$[0].startedAt").value("2023-12-01T10:00:00"))
        .andExpect(jsonPath("$[0].endedAt").value("2023-12-01T12:00:00"))
        .andExpect(jsonPath("$[1].studyId").value(2L))
        .andExpect(jsonPath("$[1].studyName").value("Java Study Group"))
        .andExpect(jsonPath("$[1].startedAt").value("2023-12-02T09:00:00"))
        .andExpect(jsonPath("$[1].endedAt").value("2023-12-02T11:00:00"));
  }
}
