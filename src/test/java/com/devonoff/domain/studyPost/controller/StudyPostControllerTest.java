package com.devonoff.domain.studyPost.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.domain.studyPost.dto.StudyPostCreateRequest;
import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.domain.studyPost.dto.StudyPostUpdateRequest;
import com.devonoff.domain.studyPost.service.StudyPostService;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
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
import org.springframework.mock.web.MockMultipartFile;
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

  @MockBean
  private AuthService authService;

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

    when(studyPostService.getStudyPostDetail(studyPostId)).thenReturn(studyPostDto);

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

  @DisplayName("스터디 모집글 상세 조회 실패 - 모집글 없음")
  @Test
  void getStudyPostDetail_NotFound() throws Exception {
    // Given
    Long studyPostId = 123L;

    when(studyPostService.getStudyPostDetail(studyPostId))
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

    when(studyPostService.searchStudyPosts(
        any(), any(), any(), any(),
        Mockito.anyInt(), any(), any(), any(), any()))
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

  @DisplayName("스터디 모집글 생성 성공")
  @Test
  void createStudyPost_Success() throws Exception {
    // Given
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "test-image.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "Test Image Content".getBytes()
    );

    StudyPostDto response = StudyPostDto.builder()
        .id(1L)
        .title("코딩 테스트 준비")
        .studyName("코테")
        .subject(StudySubject.JOB_PREPARATION)
        .difficulty(StudyDifficulty.MEDIUM)
        .dayType(List.of("월", "화"))
        .startDate(LocalDate.of(2024, 12, 10))
        .endDate(LocalDate.of(2024, 12, 20))
        .startTime(LocalTime.of(18, 0))
        .endTime(LocalTime.of(20, 0))
        .meetingType(StudyMeetingType.ONLINE)
        .recruitmentPeriod(LocalDate.of(2024, 12, 5))
        .description("코딩 테스트 스터디 모집")
        .maxParticipants(5)
        .userId(1L)
        .thumbnailImgUrl("mock_thumbnail_url")
        .build();

    when(studyPostService.createStudyPost(any(StudyPostCreateRequest.class)))
        .thenReturn(response);

    // When & Then
    mockMvc.perform(multipart("/api/study-posts")
            .file(file)
            .param("title", "코딩 테스트 준비")
            .param("studyName", "코테")
            .param("subject", "JOB_PREPARATION")
            .param("difficulty", "MEDIUM")
            .param("dayType", "월", "화")
            .param("startDate", "2024-12-10")
            .param("endDate", "2024-12-20")
            .param("startTime", "18:00")
            .param("endTime", "20:00")
            .param("meetingType", "ONLINE")
            .param("recruitmentPeriod", "2024-12-05")
            .param("description", "코딩 테스트 스터디 모집")
            .param("maxParticipants", "5")
            .param("userId", "1")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.title").value("코딩 테스트 준비"))
        .andExpect(jsonPath("$.studyName").value("코테"))
        .andExpect(jsonPath("$.subject").value("JOB_PREPARATION"))
        .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
        .andExpect(jsonPath("$.dayType[0]").value("월"))
        .andExpect(jsonPath("$.dayType[1]").value("화"))
        .andExpect(jsonPath("$.startDate").value("2024-12-10"))
        .andExpect(jsonPath("$.endDate").value("2024-12-20"))
        .andExpect(jsonPath("$.startTime").value("18:00:00"))
        .andExpect(jsonPath("$.endTime").value("20:00:00"))
        .andExpect(jsonPath("$.meetingType").value("ONLINE"))
        .andExpect(jsonPath("$.recruitmentPeriod").value("2024-12-05"))
        .andExpect(jsonPath("$.description").value("코딩 테스트 스터디 모집"))
        .andExpect(jsonPath("$.maxParticipants").value(5))
        .andExpect(jsonPath("$.userId").value(1L))
        .andExpect(jsonPath("$.thumbnailImgUrl").value("mock_thumbnail_url"));
  }

  @DisplayName("스터디 모집글 생성 실패 - 모집 인원이 잘못된 경우")
  @Test
  void createStudyPost_Fail_InvalidMaxParticipants() throws Exception {
    // Given
    Long userId = 1L;

    MockMultipartFile file = new MockMultipartFile(
        "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "Mock File Content".getBytes());

    StudyPostCreateRequest request = StudyPostCreateRequest.builder()
        .title("코딩 테스트 준비")
        .studyName("코테")
        .subject(StudySubject.JOB_PREPARATION)
        .difficulty(StudyDifficulty.MEDIUM)
        .dayType(List.of("월", "화"))
        .startDate(LocalDate.of(2024, 12, 10))
        .endDate(LocalDate.of(2024, 12, 20))
        .startTime(LocalTime.of(18, 0))
        .endTime(LocalTime.of(20, 0))
        .meetingType(StudyMeetingType.ONLINE)
        .recruitmentPeriod(LocalDate.of(2024, 12, 5))
        .description("코딩 테스트 스터디 모집")
        .maxParticipants(15) // 잘못된 모집 인원
        .userId(userId)
        .file(file)
        .build();

    when(authService.getLoginUserId()).thenReturn(userId);
    doThrow(new CustomException(ErrorCode.INVALID_MAX_PARTICIPANTS))
        .when(studyPostService).createStudyPost(any(StudyPostCreateRequest.class));

    // When & Then
    mockMvc.perform(multipart("/api/study-posts")
            .file(file)
            .param("title", request.getTitle())
            .param("studyName", request.getStudyName())
            .param("subject", request.getSubject().name())
            .param("difficulty", request.getDifficulty().name())
            .param("dayType", String.join(",", request.getDayType()))
            .param("startDate", request.getStartDate().toString())
            .param("endDate", request.getEndDate().toString())
            .param("startTime", request.getStartTime().toString())
            .param("endTime", request.getEndTime().toString())
            .param("meetingType", request.getMeetingType().name())
            .param("recruitmentPeriod", request.getRecruitmentPeriod().toString())
            .param("description", request.getDescription())
            .param("maxParticipants", String.valueOf(request.getMaxParticipants()))
            .param("userId", String.valueOf(request.getUserId()))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("모집 인원은 최소 2명, 최대 10명까지 설정할 수 있습니다."));
  }

  @DisplayName("스터디 모집글 생성 실패 - 유저가 없는 경우")
  @Test
  void createStudyPost_Fail_UserNotFound() throws Exception {
    // Given
    Long userId = 1L;

    MockMultipartFile file = new MockMultipartFile(
        "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "Mock File Content".getBytes());

    StudyPostCreateRequest request = StudyPostCreateRequest.builder()
        .title("코딩 테스트 준비")
        .studyName("코테")
        .subject(StudySubject.JOB_PREPARATION)
        .difficulty(StudyDifficulty.MEDIUM)
        .dayType(List.of("월", "화"))
        .startDate(LocalDate.of(2024, 12, 10))
        .endDate(LocalDate.of(2024, 12, 20))
        .startTime(LocalTime.of(18, 0))
        .endTime(LocalTime.of(20, 0))
        .meetingType(StudyMeetingType.ONLINE)
        .recruitmentPeriod(LocalDate.of(2024, 12, 5))
        .description("코딩 테스트 스터디 모집")
        .maxParticipants(5)
        .userId(userId)
        .file(file)
        .build();

    when(authService.getLoginUserId()).thenReturn(userId);
    doThrow(new CustomException(ErrorCode.USER_NOT_FOUND))
        .when(studyPostService).createStudyPost(any(StudyPostCreateRequest.class));

    // When & Then
    mockMvc.perform(multipart("/api/study-posts")
            .file(file)
            .param("title", request.getTitle())
            .param("studyName", request.getStudyName())
            .param("subject", request.getSubject().name())
            .param("difficulty", request.getDifficulty().name())
            .param("dayType", String.join(",", request.getDayType()))
            .param("startDate", request.getStartDate().toString())
            .param("endDate", request.getEndDate().toString())
            .param("startTime", request.getStartTime().toString())
            .param("endTime", request.getEndTime().toString())
            .param("meetingType", request.getMeetingType().name())
            .param("recruitmentPeriod", request.getRecruitmentPeriod().toString())
            .param("description", request.getDescription())
            .param("maxParticipants", String.valueOf(request.getMaxParticipants()))
            .param("userId", String.valueOf(request.getUserId()))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isNotFound())
        .andExpect(content().string("사용자를 찾을 수 없습니다."));
  }

  @DisplayName("스터디 모집글 생성 실패 - 병행 스터디에서 위도/경도 없음")
  @Test
  void createStudyPost_Fail_LocationRequiredForHybrid() throws Exception {
    // Given
    Long userId = 1L;

    MockMultipartFile file = new MockMultipartFile(
        "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "Mock File Content".getBytes());

    StudyPostCreateRequest request = StudyPostCreateRequest.builder()
        .title("코딩 테스트 준비")
        .studyName("코테")
        .subject(StudySubject.JOB_PREPARATION)
        .difficulty(StudyDifficulty.MEDIUM)
        .dayType(List.of("월", "화"))
        .startDate(LocalDate.of(2024, 12, 10))
        .endDate(LocalDate.of(2024, 12, 20))
        .startTime(LocalTime.of(18, 0))
        .endTime(LocalTime.of(20, 0))
        .meetingType(StudyMeetingType.HYBRID) // 병행 스터디
        .recruitmentPeriod(LocalDate.of(2024, 12, 5))
        .description("코딩 테스트 스터디 모집")
        .maxParticipants(5)
        .userId(userId)
        .latitude(null) // 위치 정보 없음
        .longitude(null) // 위치 정보 없음
        .file(file)
        .build();

    when(authService.getLoginUserId()).thenReturn(userId);
    doThrow(new CustomException(ErrorCode.LOCATION_REQUIRED_FOR_HYBRID))
        .when(studyPostService).createStudyPost(any(StudyPostCreateRequest.class));

    // When & Then
    mockMvc.perform(multipart("/api/study-posts")
            .file(file)
            .param("title", request.getTitle())
            .param("studyName", request.getStudyName())
            .param("subject", request.getSubject().name())
            .param("difficulty", request.getDifficulty().name())
            .param("dayType", String.join(",", request.getDayType()))
            .param("startDate", request.getStartDate().toString())
            .param("endDate", request.getEndDate().toString())
            .param("startTime", request.getStartTime().toString())
            .param("endTime", request.getEndTime().toString())
            .param("meetingType", request.getMeetingType().name())
            .param("recruitmentPeriod", request.getRecruitmentPeriod().toString())
            .param("description", request.getDescription())
            .param("maxParticipants", String.valueOf(request.getMaxParticipants()))
            .param("userId", String.valueOf(request.getUserId()))
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("온/오프라인 병행 스터디의 경우 위치 정보가 필수입니다."));
  }

  @DisplayName("스터디 모집글 수정 성공")
  @Test
  void updateStudyPost_Success() throws Exception {
    // Given
    Long studyPostId = 1L;

    StudyPostUpdateRequest updateRequest = StudyPostUpdateRequest.builder()
        .title("Updated Title")
        .studyName("Updated Study")
        .subject(StudySubject.JOB_PREPARATION)
        .difficulty(StudyDifficulty.HIGH)
        .dayType(List.of("월", "화"))
        .startDate(LocalDate.of(2024, 12, 10))
        .endDate(LocalDate.of(2024, 12, 20))
        .startTime(LocalTime.of(18, 0))
        .endTime(LocalTime.of(20, 0))
        .meetingType(StudyMeetingType.ONLINE)
        .recruitmentPeriod(LocalDate.of(2024, 12, 5))
        .description("Updated Description")
        .latitude(37.5665)
        .longitude(126.9780)
        .status(StudyPostStatus.RECRUITING)
        .thumbnailImgUrl("updated_thumbnail_url")
        .maxParticipants(10)
        .build();

    StudyPostDto updatedResponse = StudyPostDto.builder()
        .id(studyPostId)
        .title(updateRequest.getTitle())
        .studyName(updateRequest.getStudyName())
        .subject(updateRequest.getSubject())
        .difficulty(updateRequest.getDifficulty())
        .dayType(updateRequest.getDayType())
        .startDate(updateRequest.getStartDate())
        .endDate(updateRequest.getEndDate())
        .startTime(updateRequest.getStartTime())
        .endTime(updateRequest.getEndTime())
        .meetingType(updateRequest.getMeetingType())
        .recruitmentPeriod(updateRequest.getRecruitmentPeriod())
        .description(updateRequest.getDescription())
        .latitude(updateRequest.getLatitude())
        .longitude(updateRequest.getLongitude())
        .status(updateRequest.getStatus())
        .thumbnailImgUrl(updateRequest.getThumbnailImgUrl())
        .maxParticipants(updateRequest.getMaxParticipants())
        .build();

    when(studyPostService.updateStudyPost(eq(studyPostId), any(StudyPostUpdateRequest.class)))
        .thenReturn(updatedResponse);

    // When & Then
    mockMvc.perform(put("/api/study-posts/{studyPostId}", studyPostId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "Updated Title",
                  "studyName": "Updated Study",
                  "subject": "JOB_PREPARATION",
                  "difficulty": "HIGH",
                  "dayType": ["월", "화"],
                  "startDate": "2024-12-10",
                  "endDate": "2024-12-20",
                  "startTime": "18:00:00",
                  "endTime": "20:00:00",
                  "meetingType": "ONLINE",
                  "recruitmentPeriod": "2024-12-05",
                  "description": "Updated Description",
                  "latitude": 37.5665,
                  "longitude": 126.9780,
                  "status": "RECRUITING",
                  "thumbnailImgUrl": "updated_thumbnail_url",
                  "maxParticipants": 10
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(studyPostId))
        .andExpect(jsonPath("$.title").value("Updated Title"))
        .andExpect(jsonPath("$.studyName").value("Updated Study"))
        .andExpect(jsonPath("$.subject").value("JOB_PREPARATION"))
        .andExpect(jsonPath("$.difficulty").value("HIGH"))
        .andExpect(jsonPath("$.dayType[0]").value("월"))
        .andExpect(jsonPath("$.dayType[1]").value("화"))
        .andExpect(jsonPath("$.startDate").value("2024-12-10"))
        .andExpect(jsonPath("$.endDate").value("2024-12-20"))
        .andExpect(jsonPath("$.startTime").value("18:00:00"))
        .andExpect(jsonPath("$.endTime").value("20:00:00"))
        .andExpect(jsonPath("$.meetingType").value("ONLINE"))
        .andExpect(jsonPath("$.recruitmentPeriod").value("2024-12-05"))
        .andExpect(jsonPath("$.description").value("Updated Description"))
        .andExpect(jsonPath("$.latitude").value(37.5665))
        .andExpect(jsonPath("$.longitude").value(126.9780))
        .andExpect(jsonPath("$.status").value("RECRUITING"))
        .andExpect(jsonPath("$.thumbnailImgUrl").value("updated_thumbnail_url"))
        .andExpect(jsonPath("$.maxParticipants").value(10))
        .andDo(print());

    verify(studyPostService, times(1)).updateStudyPost(eq(studyPostId),
        any(StudyPostUpdateRequest.class));
  }

  @DisplayName("스터디 모집글 수정 실패 - 모집글 없음")
  @Test
  void updateStudyPost_NotFound() throws Exception {
    // Given
    Long studyPostId = 999L;

    doThrow(new CustomException(ErrorCode.STUDY_POST_NOT_FOUND))
        .when(studyPostService)
        .updateStudyPost(eq(studyPostId), any(StudyPostUpdateRequest.class));

    // When & Then
    mockMvc.perform(put("/api/study-posts/{studyPostId}", studyPostId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "title": "Non-existent Title"
                }
                """))
        .andExpect(status().isNotFound())
        .andExpect(content().string("스터디 모집글을 찾을 수 없습니다."))
        .andDo(print());

    verify(studyPostService, times(1)).updateStudyPost(eq(studyPostId),
        any(StudyPostUpdateRequest.class));
  }

  @DisplayName("스터디 모집글 모집 마감 성공")
  @Test
  void closeStudyPost_Success() throws Exception {
    // Given
    Long studyPostId = 1L;
    Long leaderId = 1L;

    when(authService.getLoginUserId()).thenReturn(leaderId);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/close", studyPostId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(print());

    verify(studyPostService, times(1)).closeStudyPost(studyPostId);
  }

  @DisplayName("스터디 모집글 모집 마감 실패 - 모집글 없음")
  @Test
  void closeStudyPost_Fail_StudyPostNotFound() throws Exception {
    // Given
    Long studyPostId = 999L;

    doThrow(new CustomException(ErrorCode.STUDY_POST_NOT_FOUND))
        .when(studyPostService).closeStudyPost(studyPostId);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/close", studyPostId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound()) // HTTP 404 상태 확인
        .andExpect(content().string("스터디 모집글을 찾을 수 없습니다."))
        .andDo(print());

    verify(studyPostService, times(1)).closeStudyPost(studyPostId);
  }

  @DisplayName("스터디 모집글 모집 마감 실패 - 모집 상태가 모집 중이 아님")
  @Test
  void closeStudyPost_Fail_InvalidStudyStatus() throws Exception {
    // Given
    Long studyPostId = 1L;

    doThrow(new CustomException(ErrorCode.INVALID_STUDY_STATUS))
        .when(studyPostService).closeStudyPost(studyPostId);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/close", studyPostId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest()) // 400
        .andExpect(content().string("잘못된 스터디 상태값입니다."))
        .andDo(print());

    verify(studyPostService, times(1)).closeStudyPost(studyPostId);
  }

  @DisplayName("스터디 모집글 마감 실패 - 승인된 신청자가 없음")
  @Test
  void closeStudyPost_Fail_NoApprovedSignups() throws Exception {
    // Given
    Long studyPostId = 1L;

    doThrow(new CustomException(ErrorCode.NO_APPROVED_SIGNUPS))
        .when(studyPostService).closeStudyPost(studyPostId);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/close", studyPostId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest()) // 400
        .andExpect(content().string("승인된 신청자가 없습니다."))
        .andDo(print());

    verify(studyPostService, times(1)).closeStudyPost(studyPostId);
  }

  @DisplayName("스터디 모집글 취소 성공")
  @Test
  void cancelStudyPost_Success() throws Exception {
    // Given
    Long studyPostId = 1L;

    doNothing().when(studyPostService).cancelStudyPost(studyPostId);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/cancel", studyPostId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(print());

    verify(studyPostService, times(1)).cancelStudyPost(studyPostId);
  }

  @DisplayName("스터디 모집글 취소 실패 - 모집글 없음")
  @Test
  void cancelStudyPost_Fail_StudyPostNotFound() throws Exception {
    // Given
    Long studyPostId = 999L;

    doThrow(new CustomException(ErrorCode.STUDY_POST_NOT_FOUND))
        .when(studyPostService).cancelStudyPost(studyPostId);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/cancel", studyPostId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound()) // 404
        .andExpect(content().string("스터디 모집글을 찾을 수 없습니다."))
        .andDo(print());

    verify(studyPostService, times(1)).cancelStudyPost(studyPostId);
  }

  @DisplayName("스터디 모집글 취소 실패 - 이미 취소된 모집글")
  @Test
  void cancelStudyPost_Fail_InvalidStudyStatus() throws Exception {
    // Given
    Long studyPostId = 1L;

    doThrow(new CustomException(ErrorCode.INVALID_STUDY_STATUS))
        .when(studyPostService).cancelStudyPost(studyPostId);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/cancel", studyPostId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest()) // 400
        .andExpect(content().string("잘못된 스터디 상태값입니다."))
        .andDo(print());

    verify(studyPostService, times(1)).cancelStudyPost(studyPostId);
  }

  @DisplayName("모집 취소된 스터디 모집 기간 연장 - 성공")
  @Test
  void extendCanceledStudy_Success() throws Exception {
    // Given
    Long studyPostId = 1L;
    LocalDate newRecruitmentPeriod = LocalDate.of(2024, 12, 20);

    doNothing().when(studyPostService).extendCanceledStudy(studyPostId, newRecruitmentPeriod);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/extend-canceled", studyPostId)
            .param("recruitmentPeriod", newRecruitmentPeriod.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(studyPostService, times(1)).extendCanceledStudy(studyPostId, newRecruitmentPeriod);
  }

  @DisplayName("모집 취소된 스터디 모집 기간 연장 실패 - 모집글 없음")
  @Test
  void extendCanceledStudy_Fail_StudyPostNotFound() throws Exception {
    // Given
    Long studyPostId = 999L;
    LocalDate newRecruitmentPeriod = LocalDate.of(2024, 12, 20);

    doThrow(new CustomException(ErrorCode.STUDY_POST_NOT_FOUND))
        .when(studyPostService).extendCanceledStudy(studyPostId, newRecruitmentPeriod);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/extend-canceled", studyPostId)
            .param("recruitmentPeriod", newRecruitmentPeriod.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().string("스터디 모집글을 찾을 수 없습니다."));

    verify(studyPostService, times(1)).extendCanceledStudy(studyPostId, newRecruitmentPeriod);
  }

  @DisplayName("모집 취소된 스터디 모집 기간 연장 실패 - 모집글 상태가 취소 상태가 아님")
  @Test
  void extendCanceledStudy_Fail_InvalidStudyStatus() throws Exception {
    // Given
    Long studyPostId = 1L;
    LocalDate newRecruitmentPeriod = LocalDate.of(2024, 12, 20);

    doThrow(new CustomException(ErrorCode.INVALID_STUDY_STATUS))
        .when(studyPostService).extendCanceledStudy(studyPostId, newRecruitmentPeriod);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/extend-canceled", studyPostId)
            .param("recruitmentPeriod", newRecruitmentPeriod.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("잘못된 스터디 상태값입니다."));

    verify(studyPostService, times(1)).extendCanceledStudy(studyPostId, newRecruitmentPeriod);
  }

  @DisplayName("모집 취소된 스터디 모집 기간 연장 실패 - 모집기간 연장 한달 초과")
  @Test
  void extendCanceledStudy_Fail_StudyExtensionFailed() throws Exception {
    // Given
    Long studyPostId = 1L;
    LocalDate newRecruitmentPeriod = LocalDate.of(2025, 1, 20);

    doThrow(new CustomException(ErrorCode.STUDY_EXTENSION_FAILED))
        .when(studyPostService).extendCanceledStudy(studyPostId, newRecruitmentPeriod);

    // When & Then
    mockMvc.perform(patch("/api/study-posts/{studyPostId}/extend-canceled", studyPostId)
            .param("recruitmentPeriod", newRecruitmentPeriod.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest()) // 상태 코드 확인
        .andExpect(content().string("스터디 모집 기한 연장은 최대 1개월입니다."));

    verify(studyPostService, times(1)).extendCanceledStudy(studyPostId, newRecruitmentPeriod);
  }
}