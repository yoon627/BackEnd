package com.devonoff.domain.student.service;

import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.repository.StudyRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository studentRepository;
  private final StudyRepository studyRepository;

  // 스터디에서 특정 참가자 삭제
  public void removeStudent(Long studentId) {
    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    Study study = student.getStudy();

    studentRepository.delete(student);

    updateTotalParticipants(study);
  }

  // totalParticipants 업데이트
  private void updateTotalParticipants(Study study) {
    int participantCount = studentRepository.countParticipantsByStudy(study);
    study.setTotalParticipants(participantCount);
    studyRepository.save(study);
  }
}
