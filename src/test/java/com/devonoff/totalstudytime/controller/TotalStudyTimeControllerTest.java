package com.devonoff.totalstudytime.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.config.SecurityConfig;
import com.devonoff.domain.totalstudytime.controller.TotalStudyTimeController;
import com.devonoff.domain.totalstudytime.dto.TotalStudyTimeDto;
import com.devonoff.domain.totalstudytime.service.TotalStudyTimeService;
import com.devonoff.util.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(TotalStudyTimeController.class)
@Import(SecurityConfig.class) // SecurityConfig를 명시적으로 포함 (Optional)
@AutoConfigureMockMvc(addFilters = false)
class TotalStudyTimeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TotalStudyTimeService totalStudyTimeService;

  @MockBean
  private JwtProvider jwtProvider;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("특정 스터디 누적 학습시간 조회 성공")
  void testGetTotalStudyTime_Success() throws Exception {
    // Given
    Long studyId = 1L;
    TotalStudyTimeDto mockTotalStudyTime = TotalStudyTimeDto.builder()
        .studyId(studyId)
        .studyName("Java Study Group")
        .totalStudyTime("2시간") // 총 학습 시간을 문자열로 변경
        .ranking(1L)
        .percent(100.0)
        .build();

    when(totalStudyTimeService.getTotalStudyTime(studyId)).thenReturn(mockTotalStudyTime);

    // When & Then
    mockMvc.perform(get("/api/total-study-time/{studyId}", studyId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.studyId").value(studyId))
        .andExpect(jsonPath("$.studyName").value("Java Study Group"))
        .andExpect(jsonPath("$.totalStudyTime").value("2시간")) // 문자열 값 확인
        .andExpect(jsonPath("$.ranking").value(1L))
        .andExpect(jsonPath("$.percent").value(100.0));
  }

  @Test
  @DisplayName("누적 학습시간 기준 랭킹 조회 성공")
  void testGetTotalStudyTimeRanking_Success() throws Exception {
    // Given
    List<TotalStudyTimeDto> mockRanking = Arrays.asList(
        TotalStudyTimeDto.builder()
            .studyId(1L)
            .studyName("Java Study Group")
            .totalStudyTime("2시간")
            .ranking(1L)
            .percent(50.0)
            .build(),
        TotalStudyTimeDto.builder()
            .studyId(2L)
            .studyName("Python Study Group")
            .totalStudyTime("1시간 30분")
            .ranking(2L)
            .percent(100.0)
            .build()
    );

    when(totalStudyTimeService.getTotalStudyTimeRanking()).thenReturn(mockRanking);

    // When & Then
    mockMvc.perform(get("/api/total-study-time/ranking")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].studyId").value(1L))
        .andExpect(jsonPath("$[0].studyName").value("Java Study Group"))
        .andExpect(jsonPath("$[0].totalStudyTime").value("2시간")) // 문자열 값 확인
        .andExpect(jsonPath("$[0].ranking").value(1L))
        .andExpect(jsonPath("$[0].percent").value(50.0))
        .andExpect(jsonPath("$[1].studyId").value(2L))
        .andExpect(jsonPath("$[1].studyName").value("Python Study Group"))
        .andExpect(jsonPath("$[1].totalStudyTime").value("1시간 30분")) // 문자열 값 확인
        .andExpect(jsonPath("$[1].ranking").value(2L))
        .andExpect(jsonPath("$[1].percent").value(100.0));
  }
}