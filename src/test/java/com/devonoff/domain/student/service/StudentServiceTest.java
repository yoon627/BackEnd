package com.devonoff.domain.student.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  @InjectMocks
  private StudentService studentService;

  @Mock
  private StudentRepository studentRepository;

  @Mock
  private StudyRepository studyRepository;

  @Test
  @DisplayName("스터디에서 특정 참가자 삭제 성공")
  void removeStudent_Success() {
    // Given
    Long studentId = 1L;
    Long studyId = 10L;

    Study study = Study.builder()
        .id(studyId)
        .totalParticipants(3)
        .build();

    Student student = Student.builder()
        .id(studentId)
        .study(study)
        .user(User.builder().id(100L).build())
        .isLeader(false)
        .build();

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentRepository.countParticipantsByStudy(study)).thenReturn(2);

    // When
    studentService.removeStudent(studentId);

    // Then
    assertEquals(2, study.getTotalParticipants());
    verify(studentRepository).findById(studentId);
    verify(studentRepository).delete(student);
    verify(studentRepository).countParticipantsByStudy(study);
    verify(studyRepository).save(study);
  }

  @Test
  @DisplayName("스터디에서 특정 참가자 삭제 실패 - 유저 없음")
  void removeStudent_Fail_UserNotFound() {
    // Given
    Long studentId = 1L;

    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        studentService.removeStudent(studentId)
    );

    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    verify(studentRepository).findById(studentId);
    verifyNoInteractions(studyRepository);
  }
}
