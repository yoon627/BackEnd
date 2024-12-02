package com.devonoff.domain.studyPost.service;

import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.service.StudyService;
import com.devonoff.domain.studyPost.dto.StudyPostCreateRequest;
import com.devonoff.domain.studyPost.dto.StudyPostCreateResponse;
import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.domain.studyPost.dto.StudyPostUpdateDto;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.domain.studySignup.repository.StudySignupRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudySignupStatus;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyPostService {

  private final StudyPostRepository studyPostRepository;
  private final UserRepository userRepository;
  private final StudySignupRepository studySignupRepository;
  private final StudentRepository studentRepository;
  private final StudyPostMapper studyPostMapper;
  private final StudyService studyService;

  // 상세 조회
  public StudyPostDto getStudyPostDetail(Long studyPostId) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    return StudyPostDto.fromEntity(studyPost);
  }

  // 조회 (검색리스트)
  public Page<StudyPostDto> searchStudyPosts(
      StudyMeetingType meetingType, String title, StudySubject subject,
      StudyDifficulty difficulty, int dayType, StudyStatus status,
      Double latitude, Double longitude, Pageable pageable) {

    return studyPostRepository.findStudyPostsByFilters(
        meetingType, title, subject, difficulty, dayType, status,
        latitude, longitude, pageable);
  }

  // 생성
  public StudyPostCreateResponse createStudyPost(StudyPostCreateRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (request.getMeetingType() == StudyMeetingType.HYBRID &&
        (request.getLatitude() == null || request.getLongitude() == null)) {
      throw new CustomException(ErrorCode.LOCATION_REQUIRED_FOR_HYBRID);
    }

    StudyPost studyPost = StudyPost.createFromRequest(request, user);
    studyPostRepository.save(studyPost);

    return new StudyPostCreateResponse("스터디 모집 글이 생성되었습니다.");
  }

  // 수정
  @Transactional
  public StudyPostUpdateDto.Response updateStudyPost(Long studyPostId,
      StudyPostUpdateDto.Request request) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    studyPostMapper.toStudyPost(request, studyPost);

    return new StudyPostUpdateDto.Response("스터디 모집 글이 업데이트되었습니다.");
  }

  // 모집 마감 -> 스터디 진행 시작
  @Transactional
  public void closeStudyPost(Long studyPostId) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    if (studyPost.getStatus() != StudyStatus.RECRUITING) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    studyPost.setStatus(StudyStatus.IN_PROGRESS);

    List<StudySignup> approvedSignups = studySignupRepository.findByStudyPostAndStatus(
        studyPost, StudySignupStatus.APPROVED);

    if (approvedSignups.isEmpty()) {
      throw new CustomException(ErrorCode.NO_APPROVED_SIGNUPS);
    }

    Study study = studyService.createStudyFromClosedPost(studyPostId);

    Student leader = Student.builder()
        .study(study)
        .user(studyPost.getUser())
        .isLeader(true)
        .build();
    studentRepository.save(leader);

    List<Student> students = approvedSignups.stream()
        .map(signup -> Student.builder()
            .study(study)
            .user(signup.getUser())
            .isLeader(false)
            .build())
        .toList();
    studentRepository.saveAll(students);
  }

  // 모집 취소 -> 사용자가 직접 취소
  @Transactional
  public void cancelStudyPost(Long studyPostId) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    if (studyPost.getStatus() == StudyStatus.IN_PROGRESS) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    studyPost.setStatus(StudyStatus.CANCELED);
  }

  // 모집 취소 -> 배치 작업으로 자동 취소
  @Transactional
  public void cancelStudyPostIfExpired() {
    LocalDate currentDate = LocalDate.now();

    List<StudyPost> studyPosts = studyPostRepository.findAllByRecruitmentPeriodBeforeAndStatus(
        currentDate, StudyStatus.RECRUITING);

    for (StudyPost studyPost : studyPosts) {
      studyPost.setStatus(StudyStatus.CANCELED);
    }
  }

  // 모집 취소된 스터디 모집 기간 연장
  @Transactional
  public void extendCanceledStudy(Long studyPostId, LocalDate newRecruitmentPeriod) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    if (!StudyStatus.CANCELED.equals(studyPost.getStatus())) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    if (newRecruitmentPeriod.isAfter(studyPost.getRecruitmentPeriod().plusMonths(1))) {
      throw new CustomException(ErrorCode.STUDY_EXTENSION_FAILED);
    }

    studyPost.setStatus(StudyStatus.RECRUITING);
    studyPost.setRecruitmentPeriod(newRecruitmentPeriod);
  }
}
