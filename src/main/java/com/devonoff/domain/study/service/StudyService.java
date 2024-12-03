package com.devonoff.domain.study.service;

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
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyStatus;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyService {

  private final StudyRepository studyRepository;
  private final StudyPostRepository studyPostRepository;
  private final TotalStudyTimeRepository totalStudyTimeRepository;
  private final StudentRepository studentRepository;

  // 모집글 마감 시 자동으로 스터디 생성
  public Study createStudyFromClosedPost(Long studyPostId) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    Study study = Study.builder()
        .studyName(studyPost.getStudyName())
        .subject(studyPost.getSubject())
        .difficulty(studyPost.getDifficulty())
        .dayType(studyPost.getDayType())
        .startDate(studyPost.getStartDate())
        .endDate(studyPost.getEndDate())
        .startTime(studyPost.getStartTime())
        .endTime(studyPost.getEndTime())
        .meetingType(studyPost.getMeetingType())
        .status(StudyStatus.IN_PROGRESS) // 기본값: 진행중
        .studyPost(studyPost)
        .studyLeader(studyPost.getUser()) // 모집글 작성자를 스터디 리더로 설정
        .build();

    Study savedStudy = studyRepository.save(study);
    totalStudyTimeRepository.save(
        TotalStudyTime.builder().studyId(savedStudy.getId()).totalStudyTime(0L).build());

    return savedStudy;
  }

  // 스터디 목록 조회
  public Page<StudyDto> getStudyList(Pageable pageable) {
    return studyRepository.findAllByOrderByCreatedAtDesc(pageable)
        .map(StudyDto::fromEntity);
  }

  // 스터디 참가자 목록 조회
  public List<StudentDto> getParticipants(Long studyId) {
    Study study = studyRepository.findById(studyId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

    List<Student> participants = studentRepository.findByStudy(study);

    return participants.stream()
        .map(StudentDto::fromEntity)
        .collect(Collectors.toList());
  }
}
