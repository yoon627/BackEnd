package com.devonoff.domain.study.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.student.dto.StudentDto;
import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.dto.StudyDto;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import com.devonoff.domain.totalstudytime.repository.TotalStudyTimeRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import java.time.LocalDate;
import com.devonoff.util.TimeProvider;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

  @Mock
  private StudyRepository studyRepository;

  @Mock
  private StudyPostRepository studyPostRepository;

  @Mock
  private TotalStudyTimeRepository totalStudyTimeRepository;

  @Mock
  private StudentRepository studentRepository;

  @Mock
  private TimeProvider timeProvider; // Mock TimeProvider

  @InjectMocks
  private StudyService studyService;

  @Mock
  private AuthService authService;

  @DisplayName("모집글 마감 시 스터디 생성 성공")
  @Test
  void createStudyFromClosedPost_Success() {
    // Given
    Long studyPostId = 1L;
    StudyPost studyPost = StudyPost.builder()
        .id(studyPostId)
        .studyName("Test Study")
        .subject(StudySubject.JOB_PREPARATION)
        .difficulty(StudyDifficulty.MEDIUM)
        .dayType(3)
        .startDate(LocalDate.of(2024, 12, 10))
        .endDate(LocalDate.of(2024, 12, 20))
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(12, 0))
        .meetingType(StudyMeetingType.ONLINE)
        .currentParticipants(5)
        .user(User.builder().id(2L).build())
        .build();

    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.of(studyPost));
    when(studyRepository.save(any(Study.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(totalStudyTimeRepository.save(any(TotalStudyTime.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Study result = studyService.createStudyFromClosedPost(studyPostId);

    // Then
    assertNotNull(result);
    assertEquals("Test Study", result.getStudyName());
    assertEquals(StudySubject.JOB_PREPARATION, result.getSubject());
    assertEquals(StudyDifficulty.MEDIUM, result.getDifficulty());
    assertEquals(3, result.getDayType());
    assertEquals(LocalDate.of(2024, 12, 10), result.getStartDate());
    assertEquals(LocalDate.of(2024, 12, 20), result.getEndDate());
    assertEquals(LocalTime.of(10, 0), result.getStartTime());
    assertEquals(LocalTime.of(12, 0), result.getEndTime());
    assertEquals(StudyMeetingType.ONLINE, result.getMeetingType());
    assertEquals(6, result.getTotalParticipants()); // currentParticipants + 1
    assertEquals(studyPost.getUser(), result.getStudyLeader());
    assertEquals(StudyStatus.PENDING, result.getStatus());

    verify(studyPostRepository, times(1)).findById(studyPostId);
    verify(studyRepository, times(1)).save(any(Study.class));
    verify(totalStudyTimeRepository, times(1)).save(any(TotalStudyTime.class));
  }

  @DisplayName("모집글 마감 시 스터디 생성 실패 - 모집글 없음")
  @Test
  void createStudyFromClosedPost_Fail_StudyPostNotFound() {
    // Given
    Long studyPostId = 999L;
    when(studyPostRepository.findById(studyPostId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class,
        () -> studyService.createStudyFromClosedPost(studyPostId));

    assertEquals(ErrorCode.STUDY_POST_NOT_FOUND, exception.getErrorCode());
    verify(studyPostRepository, times(1)).findById(studyPostId);
  }

  @DisplayName("특정 사용자가 속한 스터디 목록 조회 성공")
  @Test
  void getStudyList_Success() {
    // Given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 12);

    StudyPost studyPost1 = StudyPost.builder()
        .id(1L)
        .studyName("스터디 1")
        .status(StudyPostStatus.RECRUITING)
        .build();

    StudyPost studyPost2 = StudyPost.builder()
        .id(2L)
        .studyName("스터디 2")
        .status(StudyPostStatus.RECRUITING)
        .build();

    Study study1 = Study.builder()
        .id(1L)
        .studyName("스터디 1")
        .studyPost(studyPost1)
        .status(StudyStatus.PENDING)
        .studyLeader(new User())
        .dayType(3)
        .build();

    Study study2 = Study.builder()
        .id(2L)
        .studyName("스터디 2")
        .studyPost(studyPost2)
        .status(StudyStatus.IN_PROGRESS)
        .studyLeader(new User())
        .dayType(3)
        .build();

    List<Study> studies = List.of(study1, study2);
    Page<Study> studyPage = new PageImpl<>(studies, pageable, studies.size());

    when(studyRepository.findByStudentsUserIdOrderByCreatedAtDesc(userId, pageable))
        .thenReturn(studyPage);

    // When
    Page<StudyDto> result = studyService.getStudyList(userId, pageable);

    // Then
    assertNotNull(result, "Result should not be null");
    assertEquals(2, result.getTotalElements());
    assertEquals("스터디 1", result.getContent().get(0).getStudyName());
    assertEquals("스터디 2", result.getContent().get(1).getStudyName());
    assertEquals(2, result.getContent().size());

    verify(studyRepository, times(1))
        .findByStudentsUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  @Test
  @DisplayName("스터디 참가자 목록 조회 성공")
  void getParticipants_Success() {
    // Given
    Long studyId = 1L;

    User leader = User.builder()
        .id(1L)
        .nickname("스터디장")
        .build();

    Study study = Study.builder()
        .id(studyId)
        .studyName("테스트 스터디")
        .studyLeader(leader)
        .subject(StudySubject.JOB_PREPARATION)
        .difficulty(StudyDifficulty.MEDIUM)
        .dayType(3)
        .startDate(LocalDate.of(2024, 12, 10))
        .endDate(LocalDate.of(2024, 12, 20))
        .startTime(LocalTime.of(18, 0))
        .endTime(LocalTime.of(20, 0))
        .meetingType(StudyMeetingType.ONLINE)
        .status(StudyStatus.IN_PROGRESS)
        .totalParticipants(5)
        .build();

    User user1 = User.builder()
        .id(2L)
        .nickname("참가자1")
        .build();

    User user2 = User.builder()
        .id(3L)
        .nickname("참가자2")
        .build();

    Student student1 = Student.builder()
        .id(1L)
        .user(user1)
        .study(study)
        .isLeader(false)
        .build();

    Student student2 = Student.builder()
        .id(2L)
        .user(user2)
        .study(study)
        .isLeader(false)
        .build();

    List<Student> students = List.of(student1, student2);

    when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
    when(studentRepository.findByStudy(study)).thenReturn(students);

    // When
    List<StudentDto> result = studyService.getParticipants(studyId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("참가자1", result.get(0).getNickname());
    assertEquals("참가자2", result.get(1).getNickname());
  }

  @DisplayName("스터디 상태 자동 업데이트")
  @Test
  void testUpdateStudyStatuses() {
    // given
    LocalDateTime fixedNow = LocalDateTime.of(2024, 12, 4, 10, 0); // Mock 현재 시간
    when(timeProvider.now()).thenReturn(fixedNow); // Mock TimeProvider로 시간 고정

    // Mock 데이터
    List<Study> pendingStudies = List.of(
        Study.builder()
            .id(1L)
            .status(StudyStatus.PENDING)
            .startDate(fixedNow.toLocalDate().minusDays(1))
            .build()
    );
    List<Study> inProgressStudies = List.of(
        Study.builder()
            .id(2L)
            .status(StudyStatus.IN_PROGRESS)
            .endDate(fixedNow.toLocalDate().minusDays(1))
            .build()
    );

    when(studyRepository.findAllByStatusAndStartDateBefore(StudyStatus.PENDING, fixedNow))
        .thenReturn(pendingStudies);
    when(studyRepository.findAllByStatusAndEndDateBefore(StudyStatus.IN_PROGRESS,
        fixedNow.toLocalDate().atStartOfDay()))
        .thenReturn(inProgressStudies);

    // when
    studyService.updateStudyStatuses();

    // then
    verify(studyRepository).findAllByStatusAndStartDateBefore(StudyStatus.PENDING, fixedNow);
    verify(studyRepository).findAllByStatusAndEndDateBefore(StudyStatus.IN_PROGRESS,
        fixedNow.toLocalDate().atStartOfDay());
    verify(studyRepository).saveAll(pendingStudies);
    verify(studyRepository).saveAll(inProgressStudies);
  }
}