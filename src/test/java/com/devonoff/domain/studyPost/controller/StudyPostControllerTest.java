package com.devonoff.domain.studyPost.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.domain.studyPost.service.StudyPostService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudySubject;
import com.devonoff.util.JwtProvider;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StudyPostController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
class StudyPostControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private StudyPostService studyPostService;

  @MockBean
  private JwtProvider jwtProvider;

  @DisplayName("스터디 모집글 상세 조회 성공")
  @Test
  void getStudyPostDetail_Success() throws Exception {
    Long studyPostId = 1L;

    StudyPostDto studyPostDto = new StudyPostDto();
    studyPostDto.setId(studyPostId);
    studyPostDto.setTitle("스터디 모집글! 상세 조회 테스트");
    studyPostDto.setStudyName("코테");
    studyPostDto.setSubject(StudySubject.JOB_PREPARATION);
    studyPostDto.setDifficulty(StudyDifficulty.HIGH);
    studyPostDto.setDayType(List.of("월", "화"));
    studyPostDto.setStartDate(LocalDate.parse("2024-12-04"));
    studyPostDto.setEndDate(LocalDate.parse("2024-12-22"));
    studyPostDto.setStartTime(LocalTime.parse("19:00"));
    studyPostDto.setEndTime(LocalTime.parse("21:00"));
    studyPostDto.setMeetingType(StudyMeetingType.HYBRID);
    studyPostDto.setRecruitmentPeriod(LocalDate.parse("2024-11-30"));
    studyPostDto.setDescription("코테 공부할사람 모여");
    studyPostDto.setLatitude(35.6895);
    studyPostDto.setLongitude(139.6917);
    studyPostDto.setMaxParticipants(5);
    studyPostDto.setUserId(11L);

    Mockito.when(studyPostService.getStudyPostDetail(studyPostId)).thenReturn(studyPostDto);

    mockMvc.perform(get("/api/study-posts/{studyPostId}", studyPostId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(studyPostId))
        .andExpect(jsonPath("$.title").value("스터디 모집글! 상세 조회 테스트"))
        .andExpect(jsonPath("$.studyName").value("코테"))
        .andExpect(jsonPath("$.subject").value("JOB_PREPARATION"))
        .andExpect(jsonPath("$.difficulty").value("HIGH"))
        .andExpect(jsonPath("$.dayType", contains("월", "화")))
        .andExpect(jsonPath("$.startDate").value("2024-12-04"))
        .andExpect(jsonPath("$.endDate").value("2024-12-22"))
        .andExpect(jsonPath("$.startTime").value("19:00:00"))
        .andExpect(jsonPath("$.endTime").value("21:00:00"))
        .andExpect(jsonPath("$.meetingType").value("HYBRID"))
        .andExpect(jsonPath("$.recruitmentPeriod").value("2024-11-30"))
        .andExpect(jsonPath("$.description").value("코테 공부할사람 모여"))
        .andExpect(jsonPath("$.latitude").value(35.6895))
        .andExpect(jsonPath("$.longitude").value(139.6917))
        .andExpect(jsonPath("$.maxParticipants").value(5))
        .andExpect(jsonPath("$.userId").value(11L));
  }

  @DisplayName("스터디 모집글 상세 조회 실패")
  @Test
  void getStudyPostDetail_NotFound() throws Exception {
    // Given
    Long studyPostId = 123L;

    Mockito.when(studyPostService.getStudyPostDetail(studyPostId))
        .thenThrow(new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    // When & Then
    mockMvc.perform(get("/api/study-posts/{studyPostId}", studyPostId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @DisplayName("스터디 모집글 검색 성공")
  @Test
  void searchStudyPosts_Success() throws Exception {
    // Given
    StudyPostDto studyPostDto = new StudyPostDto();
    studyPostDto.setId(1L);
    studyPostDto.setTitle("코딩 테스트 준비");
    studyPostDto.setStudyName("코테");
    studyPostDto.setSubject(StudySubject.JOB_PREPARATION);
    studyPostDto.setDifficulty(StudyDifficulty.MEDIUM);
    studyPostDto.setDayType(List.of("월", "화"));
    studyPostDto.setStartDate(LocalDate.parse("2024-12-04"));
    studyPostDto.setEndDate(LocalDate.parse("2024-12-22"));
    studyPostDto.setStartTime(LocalTime.parse("19:00"));
    studyPostDto.setEndTime(LocalTime.parse("21:00"));
    studyPostDto.setMeetingType(StudyMeetingType.ONLINE);
    studyPostDto.setRecruitmentPeriod(LocalDate.parse("2024-11-30"));
    studyPostDto.setDescription("코테 공부할사람 모여");
    studyPostDto.setLatitude(37.5665);
    studyPostDto.setLongitude(126.9780);
    studyPostDto.setMaxParticipants(5);
    studyPostDto.setUserId(11L);

    Page<StudyPostDto> mockPage = new PageImpl<>(List.of(studyPostDto), PageRequest.of(0, 20), 1);

    Mockito.when(studyPostService.searchStudyPosts(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(mockPage);

    // When & Then
    mockMvc.perform(get("/api/study-posts/search")
            .param("meetingType", "ONLINE")
            .param("title", "코테")
            .param("subject", "JOB_PREPARATION")
            .param("difficulty", "MEDIUM")
            .param("dayType", "3")
            .param("status", "RECRUITING")
            .param("latitude", "37.5665")
            .param("longitude", "126.9780")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id").value(1L))
        .andExpect(jsonPath("$.content[0].title").value("코딩 테스트 준비"))
        .andExpect(jsonPath("$.content[0].studyName").value("코테"))
        .andExpect(jsonPath("$.content[0].subject").value("JOB_PREPARATION"))
        .andExpect(jsonPath("$.content[0].difficulty").value("MEDIUM"))
        .andExpect(jsonPath("$.content[0].dayType[0]").value("월"))
        .andExpect(jsonPath("$.content[0].dayType[1]").value("화"))
        .andExpect(jsonPath("$.content[0].startDate").value("2024-12-04"))
        .andExpect(jsonPath("$.content[0].endDate").value("2024-12-22"))
        .andExpect(jsonPath("$.content[0].startTime").value("19:00:00"))
        .andExpect(jsonPath("$.content[0].endTime").value("21:00:00"))
        .andExpect(jsonPath("$.content[0].meetingType").value("ONLINE"))
        .andExpect(jsonPath("$.content[0].recruitmentPeriod").value("2024-11-30"))
        .andExpect(jsonPath("$.content[0].description").value("코테 공부할사람 모여"))
        .andExpect(jsonPath("$.content[0].latitude").value(37.5665))
        .andExpect(jsonPath("$.content[0].longitude").value(126.9780))
        .andExpect(jsonPath("$.content[0].maxParticipants").value(5))
        .andExpect(jsonPath("$.content[0].userId").value(11L));
  }
}