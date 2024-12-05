package com.devonoff.domain.studyPost.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.studyPost.dto.StudyPostCreateRequest;
import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.domain.studyPost.dto.StudyPostUpdateRequest;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySubject;
import com.devonoff.util.DayTypeUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class StudyPostServiceTest {

  @Mock
  private StudyPostRepository studyPostRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PhotoService photoService;

  @Mock
  private AuthService authService;

  @InjectMocks
  private StudyPostService studyPostService;

  @DisplayName("스터디 모집글 상세 조회 성공")
  @Test
  void getStudyPostDetail_Success() {
    // Given
    Long studyPostId = 1L;

    User user = new User();
    user.setId(11L);

    StudyPost studyPost = new StudyPost();
    studyPost.setId(studyPostId);
    studyPost.setTitle("스터디 모집글! 상세 조회 테스트");
    studyPost.setStudyName("코테");
    studyPost.setSubject(StudySubject.JOB_PREPARATION);
    studyPost.setDifficulty(StudyDifficulty.HIGH);
    studyPost.setDayType(3);
    studyPost.setStartDate(LocalDate.parse("2024-12-04"));
    studyPost.setEndDate(LocalDate.parse("2024-12-22"));
    studyPost.setStartTime(LocalTime.parse("19:00"));
    studyPost.setEndTime(LocalTime.parse("21:00"));
    studyPost.setMeetingType(StudyMeetingType.HYBRID);
    studyPost.setRecruitmentPeriod(LocalDate.parse("2024-11-30"));
    studyPost.setDescription("코테 공부할사람 모여");
    studyPost.setLatitude(35.6895);
    studyPost.setLongitude(139.6917);
    studyPost.setMaxParticipants(5);
    studyPost.setUser(user);

    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));

    // When
    StudyPostDto result = studyPostService.getStudyPostDetail(studyPostId);

    // Then
    assertNotNull(result);
    assertEquals(studyPostId, result.getId());
    assertEquals("스터디 모집글! 상세 조회 테스트", result.getTitle());
    assertEquals("코테", result.getStudyName());
    assertEquals(StudySubject.JOB_PREPARATION, result.getSubject());
    assertEquals(StudyDifficulty.HIGH, result.getDifficulty());
    assertEquals(List.of("월", "화"), result.getDayType());
    assertEquals(LocalDate.parse("2024-12-04"), result.getStartDate());
    assertEquals(LocalDate.parse("2024-12-22"), result.getEndDate());
    assertEquals(LocalTime.parse("19:00"), result.getStartTime());
    assertEquals(LocalTime.parse("21:00"), result.getEndTime());
    assertEquals(StudyMeetingType.HYBRID, result.getMeetingType());
    assertEquals(LocalDate.parse("2024-11-30"), result.getRecruitmentPeriod());
    assertEquals("코테 공부할사람 모여", result.getDescription());
    assertEquals(35.6895, result.getLatitude());
    assertEquals(139.6917, result.getLongitude());
    assertEquals(5, result.getMaxParticipants());
    assertEquals(11L, result.getUserId());
  }

  @DisplayName("스터디 모집글 상세 조회 실패 - 모집글 없음")
  @Test
  void getStudyPostDetail_NotFound() {
    // Given
    Long studyPostId = 123L;

    // Optional.empty()를 반환하도록 설정
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class,
        () -> studyPostService.getStudyPostDetail(studyPostId));

    assertEquals(ErrorCode.STUDY_POST_NOT_FOUND, exception.getErrorCode());
  }

  @DisplayName("스터디 모집글 검색 성공")
  @Test
  void searchStudyPosts_Success() {
    // Given
    StudyMeetingType meetingType = StudyMeetingType.ONLINE;
    String title = "코테";
    StudySubject subject = StudySubject.JOB_PREPARATION;
    StudyDifficulty difficulty = StudyDifficulty.MEDIUM;
    int dayType = 3; // 월, 화
    StudyPostStatus status = StudyPostStatus.RECRUITING;
    Double latitude = 37.5665;
    Double longitude = 126.9780;
    Pageable pageable = PageRequest.of(0, 20);

    // 데이터 생성
    StudyPostDto studyPostDto = new StudyPostDto();
    studyPostDto.setId(1L);
    studyPostDto.setTitle("코딩 테스트 준비");
    studyPostDto.setStudyName("코테");
    studyPostDto.setSubject(StudySubject.JOB_PREPARATION);
    studyPostDto.setDifficulty(StudyDifficulty.MEDIUM);

    Page<StudyPostDto> mockPage = new PageImpl<>(List.of(studyPostDto), pageable, 1);

    // When
    Mockito.when(
            studyPostRepository.findStudyPostsByFilters(Mockito.eq(meetingType), Mockito.eq(title),
                Mockito.eq(subject), Mockito.eq(difficulty), Mockito.eq(dayType), Mockito.eq(status),
                Mockito.eq(latitude), Mockito.eq(longitude), Mockito.eq(pageable)))
        .thenReturn(mockPage);

    // When
    Page<StudyPostDto> result = studyPostService.searchStudyPosts(meetingType, title, subject,
        difficulty, dayType, status, latitude, longitude, pageable);

    // Then
    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.getTotalElements(), "Total elements should match");
    assertEquals("코딩 테스트 준비", result.getContent().get(0).getTitle(),
        "Title should match");
    assertEquals("코테", result.getContent().get(0).getStudyName(),
        "Study name should match");
    assertEquals(StudySubject.JOB_PREPARATION, result.getContent().get(0).getSubject(),
        "Subject should match");
    assertEquals(StudyDifficulty.MEDIUM, result.getContent().get(0).getDifficulty(),
        "Difficulty should match");
  }

  @DisplayName("스터디 모집글 생성 성공")
  @Test
  void createStudyPost_Success() {
    // Given
    Long loggedInUserId = 1L; // 현재 사용자
    // 요청 사용자
    MultipartFile mockFile = mock(MultipartFile.class);

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
        .latitude(null)
        .longitude(null)
        .maxParticipants(5)
        .userId(loggedInUserId)
        .file(mockFile)
        .build();

    User user = new User();
    user.setId(loggedInUserId);

    StudyPost studyPost = StudyPost.builder()
        .id(1L)
        .title(request.getTitle())
        .studyName(request.getStudyName())
        .subject(request.getSubject())
        .difficulty(request.getDifficulty())
        .dayType(DayTypeUtils.encodeDaysFromRequest(request.getDayType()))
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .startTime(request.getStartTime())
        .endTime(request.getEndTime())
        .meetingType(request.getMeetingType())
        .recruitmentPeriod(request.getRecruitmentPeriod())
        .description(request.getDescription())
        .maxParticipants(request.getMaxParticipants())
        .user(user)
        .status(null) // 기본값
        .thumbnailImgUrl("mock_thumbnail_url")
        .build();

    Mockito.when(authService.getLoginUserId()).thenReturn(loggedInUserId);

    Mockito.when(userRepository.findById(loggedInUserId)).thenReturn(Optional.of(user));
    Mockito.when(photoService.save(mockFile)).thenReturn("mock_thumbnail_url");
    Mockito.when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost);

    // When
    StudyPostDto result = studyPostService.createStudyPost(request);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertEquals(request.getTitle(), result.getTitle());
    Assertions.assertEquals(request.getStudyName(), result.getStudyName());
    Assertions.assertEquals(request.getSubject(), result.getSubject());
    Assertions.assertEquals(request.getDifficulty(), result.getDifficulty());
    Assertions.assertEquals(request.getDayType(), result.getDayType());
    Assertions.assertEquals(request.getStartDate(), result.getStartDate());
    Assertions.assertEquals(request.getEndDate(), result.getEndDate());
    Assertions.assertEquals(request.getStartTime(), result.getStartTime());
    Assertions.assertEquals(request.getEndTime(), result.getEndTime());
    Assertions.assertEquals(request.getMeetingType(), result.getMeetingType());
    Assertions.assertEquals(request.getRecruitmentPeriod(), result.getRecruitmentPeriod());
    Assertions.assertEquals(request.getDescription(), result.getDescription());
    Assertions.assertEquals(loggedInUserId, result.getUserId());
    Assertions.assertEquals("mock_thumbnail_url", result.getThumbnailImgUrl());

    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(loggedInUserId);
    verify(photoService, times(1)).save(mockFile);
    verify(studyPostRepository, times(1)).save(any(StudyPost.class));
  }

  @DisplayName("스터디 모집글 생성 실패 - 모집 인원이 잘못된 경우")
  @Test
  void createStudyPost_Fail_InvalidMaxParticipants() {
    // Given
    Long userId = 1L;
    MultipartFile mockFile = mock(MultipartFile.class);

    when(authService.getLoginUserId()).thenReturn(userId);

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
        .file(mockFile)
        .build();

    // When & Then
    CustomException exception = assertThrows(CustomException.class,
        () -> studyPostService.createStudyPost(request));

    assertEquals(ErrorCode.INVALID_MAX_PARTICIPANTS, exception.getErrorCode());
  }

  @DisplayName("스터디 모집글 생성 실패 - 유저가 없는 경우")
  @Test
  void createStudyPost_Fail_UserNotFound() {
    // Given
    Long userId = 1L;
    MultipartFile mockFile = mock(MultipartFile.class);

    when(authService.getLoginUserId()).thenReturn(userId);

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
        .file(mockFile)
        .build();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class,
        () -> studyPostService.createStudyPost(request));

    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @DisplayName("스터디 모집글 생성 실패 - 병행 스터디에서 위도/경도 없음")
  @Test
  void createStudyPost_Fail_LocationRequiredForHybrid() {
    // Given
    Long userId = 1L;
    MultipartFile mockFile = mock(MultipartFile.class);

    when(authService.getLoginUserId()).thenReturn(userId);

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
        .file(mockFile)
        .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

    // When & Then
    CustomException exception = assertThrows(CustomException.class,
        () -> studyPostService.createStudyPost(request));

    assertEquals(ErrorCode.LOCATION_REQUIRED_FOR_HYBRID, exception.getErrorCode());
  }

  @DisplayName("스터디 모집글 수정 성공")
  @Test
  void updateStudyPost_Success() {
    // Given
    Long studyPostId = 1L;
    Long loggedInUserId = 1L;

    User user = new User();
    user.setId(loggedInUserId);

    StudyPost studyPost = new StudyPost();
    studyPost.setId(studyPostId);
    studyPost.setUser(user);

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

    when(authService.getLoginUserId()).thenReturn(loggedInUserId);
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));
    when(studyPostRepository.save(any(StudyPost.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    // When
    StudyPostDto result = studyPostService.updateStudyPost(studyPostId, updateRequest);

    // Then
    assertNotNull(result);
    assertEquals(updateRequest.getTitle(), result.getTitle());
    assertEquals(updateRequest.getStudyName(), result.getStudyName());
    assertEquals(updateRequest.getSubject(), result.getSubject());
    assertEquals(updateRequest.getDifficulty(), result.getDifficulty());
    assertEquals(updateRequest.getDayType(), result.getDayType());
    assertEquals(updateRequest.getStartDate(), result.getStartDate());
    assertEquals(updateRequest.getEndDate(), result.getEndDate());
    assertEquals(updateRequest.getStartTime(), result.getStartTime());
    assertEquals(updateRequest.getEndTime(), result.getEndTime());
    assertEquals(updateRequest.getMeetingType(), result.getMeetingType());
    assertEquals(updateRequest.getRecruitmentPeriod(), result.getRecruitmentPeriod());
    assertEquals(updateRequest.getDescription(), result.getDescription());
    assertEquals(updateRequest.getLatitude(), result.getLatitude());
    assertEquals(updateRequest.getLongitude(), result.getLongitude());
    assertEquals(updateRequest.getStatus(), result.getStatus());
    assertEquals(updateRequest.getThumbnailImgUrl(), result.getThumbnailImgUrl());
    assertEquals(updateRequest.getMaxParticipants(), result.getMaxParticipants());

    verify(authService, times(1)).getLoginUserId();
    verify(studyPostRepository, times(1)).findById(studyPostId);
    verify(studyPostRepository, times(1)).save(any(StudyPost.class));
  }

  @DisplayName("스터디 모집글 수정 실패 - 모집글 없음")
  @Test
  void updateStudyPost_NotFound() {
    // Given
    Long studyPostId = 1L;
    StudyPostUpdateRequest updateRequest = new StudyPostUpdateRequest();

    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class,
        () -> studyPostService.updateStudyPost(studyPostId, updateRequest));

    assertEquals(ErrorCode.STUDY_POST_NOT_FOUND, exception.getErrorCode());
    verify(studyPostRepository, times(1)).findById(studyPostId);
    verify(studyPostRepository, never()).save(any(StudyPost.class));
  }
}