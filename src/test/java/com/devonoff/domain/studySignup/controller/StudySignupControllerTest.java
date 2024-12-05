package com.devonoff.domain.studySignup.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devonoff.domain.studySignup.dto.StudySignupCreateRequest;
import com.devonoff.domain.studySignup.dto.StudySignupDto;
import com.devonoff.domain.studySignup.service.StudySignupService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudySignupStatus;
import com.devonoff.util.JwtProvider;
import java.util.List;
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

  @Test
  @DisplayName("스터디 신청 실패 - 모집글 없음")
  void createStudySignup_Fail_StudyPostNotFound() throws Exception {
    // Given
    when(studySignupService.createStudySignup(any(StudySignupCreateRequest.class)))
        .thenThrow(new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    // When & Then
    mockMvc.perform(post("/api/study-signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"studyPostId\": 100, \"userId\": 1}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$").value("스터디 모집글을 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("스터디 신청 실패 - 모집글이 모집 중이 아님")
  void createStudySignup_Fail_InvalidStudyStatus() throws Exception {
    // Given
    when(studySignupService.createStudySignup(any(StudySignupCreateRequest.class)))
        .thenThrow(new CustomException(ErrorCode.INVALID_STUDY_STATUS));

    // When & Then
    mockMvc.perform(post("/api/study-signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"studyPostId\": 100, \"userId\": 1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$").value("잘못된 스터디 상태값입니다."));
  }

  @Test
  @DisplayName("스터디 신청 실패 - 유저 없음")
  void createStudySignup_Fail_UserNotFound() throws Exception {
    // Given
    when(studySignupService.createStudySignup(any(StudySignupCreateRequest.class)))
        .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

    // When & Then
    mockMvc.perform(post("/api/study-signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"studyPostId\": 100, \"userId\": 1}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$").value("사용자를 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("스터디 신청 실패 - 이미 해당 스터디에 신청함")
  void createStudySignup_Fail_DuplicateApplication() throws Exception {
    // Given
    when(studySignupService.createStudySignup(any(StudySignupCreateRequest.class)))
        .thenThrow(new CustomException(ErrorCode.DUPLICATE_APPLICATION));

    // When & Then
    mockMvc.perform(post("/api/study-signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"studyPostId\": 100, \"userId\": 1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$").value("이미 해당 스터디에 신청했습니다."));
  }

  @Test
  @DisplayName("신청 상태 관리 성공 - 승인")
  void updateSignupStatus_Approve_Success() throws Exception {
    // Given
    Long studySignupId = 1L;
    StudySignupStatus newStatus = StudySignupStatus.APPROVED;

    // When & Then
    mockMvc.perform(patch("/api/study-signup/{studySignupId}", studySignupId)
            .param("newStatus", newStatus.name()))
        .andExpect(status().isOk());

    verify(studySignupService, times(1)).updateSignupStatus(anyLong(),
        any(StudySignupStatus.class));
  }

  @Test
  @DisplayName("신청 상태 관리 성공 - 취소")
  void updateSignupStatus_Reject_Success() throws Exception {
    // Given
    Long studySignupId = 1L;
    StudySignupStatus newStatus = StudySignupStatus.REJECTED;

    // When & Then
    mockMvc.perform(patch("/api/study-signup/{studySignupId}", studySignupId)
            .param("newStatus", newStatus.name()))
        .andExpect(status().isOk());

    verify(studySignupService, times(1)).updateSignupStatus(anyLong(),
        any(StudySignupStatus.class));
  }

  @Test
  @DisplayName("신청 상태 관리 실패 - 신청 내역 없음")
  void updateSignupStatus_Fail_SignupNotFound() throws Exception {
    // Given
    Long studySignupId = 1L;
    StudySignupStatus newStatus = StudySignupStatus.APPROVED;

    doThrow(new CustomException(ErrorCode.SIGNUP_NOT_FOUND))
        .when(studySignupService).updateSignupStatus(anyLong(), any(StudySignupStatus.class));

    // When & Then
    mockMvc.perform(patch("/api/study-signup/{studySignupId}", studySignupId)
            .param("newStatus", newStatus.name()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$").value("스터디 신청 내역을 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("신청 상태 관리 실패 - 모집 상태가 모집 중이 아님")
  void updateSignupStatus_Fail_InvalidStudyStatus() throws Exception {
    // Given
    Long studySignupId = 1L;
    StudySignupStatus newStatus = StudySignupStatus.APPROVED;

    doThrow(new CustomException(ErrorCode.INVALID_STUDY_STATUS))
        .when(studySignupService).updateSignupStatus(anyLong(), any(StudySignupStatus.class));

    // When & Then
    mockMvc.perform(patch("/api/study-signup/{studySignupId}", studySignupId)
            .param("newStatus", newStatus.name()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$").value("잘못된 스터디 상태값입니다."));
  }

  @Test
  @DisplayName("신청 상태 관리 실패 - 이미 확정된 상태")
  void updateSignupStatus_Fail_StatusAlreadyFinalized() throws Exception {
    // Given
    Long studySignupId = 1L;
    StudySignupStatus newStatus = StudySignupStatus.APPROVED;

    doThrow(new CustomException(ErrorCode.SIGNUP_STATUS_ALREADY_FINALIZED))
        .when(studySignupService).updateSignupStatus(anyLong(), any(StudySignupStatus.class));

    // When & Then
    mockMvc.perform(patch("/api/study-signup/{studySignupId}", studySignupId)
            .param("newStatus", newStatus.name()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$").value("이미 확정된 신청 상태입니다."));
  }

  @Test
  @DisplayName("신청 목록 조회 성공")
  void getSignupList_Success() throws Exception {
    // Given
    Long studyPostId = 1L;
    StudySignupStatus filterStatus = StudySignupStatus.PENDING;

    List<StudySignupDto> studySignupList = List.of(
        StudySignupDto.builder()
            .signupId(10L)
            .userId(101L)
            .nickName("참가자1")
            .status(StudySignupStatus.PENDING)
            .build(),
        StudySignupDto.builder()
            .signupId(11L)
            .userId(102L)
            .nickName("참가자2")
            .status(StudySignupStatus.PENDING)
            .build()
    );

    when(studySignupService.getSignupList(studyPostId, filterStatus)).thenReturn(studySignupList);

    // When & Then
    mockMvc.perform(get("/api/study-signup")
            .param("studyPostId", studyPostId.toString())
            .param("status", filterStatus.name()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()").value(2))
        .andExpect(jsonPath("$[0].signupId").value(10L))
        .andExpect(jsonPath("$[0].nickName").value("참가자1"))
        .andExpect(jsonPath("$[1].signupId").value(11L))
        .andExpect(jsonPath("$[1].nickName").value("참가자2"));

    verify(studySignupService).getSignupList(studyPostId, filterStatus);
  }

  @Test
  @DisplayName("신청 목록 조회 실패 - 모집글 없음")
  void getSignupList_Fail_StudyPostNotFound() throws Exception {
    // Given
    Long studyPostId = 1L;
    StudySignupStatus filterStatus = StudySignupStatus.PENDING;

    doThrow(new CustomException(ErrorCode.STUDY_POST_NOT_FOUND))
        .when(studySignupService).getSignupList(anyLong(), any(StudySignupStatus.class));

    // When & Then
    mockMvc.perform(get("/api/study-signup")
            .param("studyPostId", studyPostId.toString())
            .param("status", filterStatus.name()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$").value("스터디 모집글을 찾을 수 없습니다."));

    verify(studySignupService).getSignupList(studyPostId, filterStatus);
  }

  @Test
  @DisplayName("신청 취소 성공")
  void cancelSignup_Success() throws Exception {
    // Given
    Long studySignupId = 1L;

    doNothing().when(studySignupService).cancelSignup(studySignupId);

    // When & Then
    mockMvc.perform(delete("/api/study-signup/{studySignupId}", studySignupId))
        .andExpect(status().isOk());

    verify(studySignupService).cancelSignup(studySignupId);
  }

  @Test
  @DisplayName("신청 취소 실패 - 신청 내역 없음")
  void cancelSignup_Fail_SignupNotFound() throws Exception {
    // Given
    Long studySignupId = 1L;

    doThrow(new CustomException(ErrorCode.SIGNUP_NOT_FOUND))
        .when(studySignupService).cancelSignup(studySignupId);

    // When & Then
    mockMvc.perform(delete("/api/study-signup/{studySignupId}", studySignupId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$").value("스터디 신청 내역을 찾을 수 없습니다."));

    verify(studySignupService).cancelSignup(studySignupId);
  }
}
