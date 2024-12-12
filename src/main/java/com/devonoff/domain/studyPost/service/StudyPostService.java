package com.devonoff.domain.studyPost.service;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.service.StudyService;
import com.devonoff.domain.studyPost.dto.StudyPostCreateRequest;
import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.domain.studyPost.dto.StudyPostUpdateRequest;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.domain.studySignup.repository.StudySignupRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySignupStatus;
import com.devonoff.type.StudySubject;
import com.devonoff.util.DayTypeUtils;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StudyPostService {

  private final StudyPostRepository studyPostRepository;
  private final UserRepository userRepository;
  private final StudySignupRepository studySignupRepository;
  private final StudentRepository studentRepository;
  private final StudyService studyService;
  private final AuthService authService;
  private final PhotoService photoService;
  @Value("${cloud.aws.s3.default-thumbnail-image-url}")
  private String defaultThumbnailImageUrl;

  // 상세 조회
  public StudyPostDto getStudyPostDetail(Long studyPostId) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    return StudyPostDto.fromEntity(studyPost);
  }

  // 상세 조회(userId)
  public Page<StudyPostDto> getStudyPostsByUserId(Long userId, Pageable pageable) {
    if (!userRepository.existsById(userId)) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    Page<StudyPost> studyPosts = studyPostRepository.findByUserId(userId, pageable);

    return studyPosts.map(StudyPostDto::fromEntity);
  }

  // 조회 (검색리스트)
  public Page<StudyPostDto> searchStudyPosts(StudyMeetingType meetingType, String title,
      StudySubject subject, StudyDifficulty difficulty, int dayType, StudyPostStatus status,
      Double latitude, Double longitude, Pageable pageable) {

    return studyPostRepository.findStudyPostsByFilters(meetingType, title, subject, difficulty,
        dayType, status, latitude, longitude, pageable);
  }

  // 생성
  @Transactional
  public StudyPostDto createStudyPost(StudyPostCreateRequest request) {
    validateUserRequestOwnership(request.getUserId());

    if (request.getMaxParticipants() < 2 || request.getMaxParticipants() > 10) {
      throw new CustomException(ErrorCode.INVALID_MAX_PARTICIPANTS);
    }

    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    if (request.getMeetingType() == StudyMeetingType.HYBRID && (request.getLatitude() == null
        || request.getLongitude() == null || request.getAddress() == null)) {
      throw new CustomException(ErrorCode.LOCATION_REQUIRED_FOR_HYBRID);
    }
    MultipartFile file = request.getFile();
    if (file != null && !file.isEmpty()) {
      String savedImgUrl = photoService.save(file);
      request.setThumbnailImgUrl(savedImgUrl);
    } else {
      request.setThumbnailImgUrl(defaultThumbnailImageUrl);
    }

    StudyPost studyPost = buildStudyPost(request, user);
    studyPostRepository.save(studyPost);

    return StudyPostDto.fromEntity(studyPost);
  }

  // 수정
  @Transactional
  public StudyPostDto updateStudyPost(Long studyPostId, StudyPostUpdateRequest request) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    validateStudyPostOwnership(studyPost.getUser().getId());

    MultipartFile file = request.getFile();
    String originImgUrl = studyPost.getThumbnailImgUrl();
    String requestImgUrl = request.getThumbnailImgUrl();

    studyPost.setTitle(request.getTitle());
    studyPost.setStudyName(request.getStudyName());
    studyPost.setSubject(request.getSubject());
    studyPost.setDifficulty(request.getDifficulty());
    studyPost.setDayType(DayTypeUtils.encodeDaysFromRequest(request.getDayType()));
    studyPost.setStartDate(request.getStartDate());
    studyPost.setEndDate(request.getEndDate());
    studyPost.setStartTime(request.getStartTime());
    studyPost.setEndTime(request.getEndTime());
    studyPost.setMeetingType(request.getMeetingType());
    studyPost.setRecruitmentPeriod(request.getRecruitmentPeriod());
    studyPost.setDescription(request.getDescription());
    studyPost.setLatitude(request.getLatitude());
    studyPost.setLongitude(request.getLongitude());
    studyPost.setStatus(request.getStatus());
    //파일이 있는 경우
    if (file != null && !file.isEmpty()) {
      photoService.delete(originImgUrl);
      studyPost.setThumbnailImgUrl(photoService.save(file));
    } else {
      //파일이 없는 경우
      if (requestImgUrl != null && !requestImgUrl.isEmpty() && requestImgUrl.equals(
          defaultThumbnailImageUrl)) {
        photoService.delete(originImgUrl);
        studyPost.setThumbnailImgUrl(defaultThumbnailImageUrl);
      }
    }

    studyPost.setMaxParticipants(request.getMaxParticipants());

    studyPostRepository.save(studyPost);

    return StudyPostDto.fromEntity(studyPost);
  }

  // 모집 마감 -> 스터디 진행 시작
  @Transactional
  public void closeStudyPost(Long studyPostId) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    validateStudyPostOwnership(studyPost.getUser().getId());

    if (studyPost.getStatus() != StudyPostStatus.RECRUITING) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    studyPost.setStatus(StudyPostStatus.CLOSED);

    List<StudySignup> approvedSignups = studySignupRepository.findByStudyPostAndStatus(studyPost,
        StudySignupStatus.APPROVED);

    if (approvedSignups.isEmpty()) {
      throw new CustomException(ErrorCode.NO_APPROVED_SIGNUPS);
    }

    Study study = studyService.createStudyFromClosedPost(studyPostId);

    Student leader = Student.builder().study(study).user(studyPost.getUser()).isLeader(true)
        .build();
    studentRepository.save(leader);

    List<Student> students = approvedSignups.stream().map(
            signup -> Student.builder().study(study).user(signup.getUser()).isLeader(false).build())
        .toList();
    studentRepository.saveAll(students);
  }

  // 모집 취소 -> 사용자가 직접 취소
  public void cancelStudyPost(Long studyPostId) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    validateStudyPostOwnership(studyPost.getUser().getId());

    if (studyPost.getStatus() == StudyPostStatus.CLOSED) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    studyPost.setStatus(StudyPostStatus.CANCELED);
    studyPostRepository.save(studyPost);
  }

  // 모집 취소 -> 배치 작업으로 자동 취소
  @Transactional
  public void cancelStudyPostIfExpired() {
    LocalDate currentDate = LocalDate.now();

    List<StudyPost> studyPosts = studyPostRepository.findAllByRecruitmentPeriodBeforeAndStatus(
        currentDate, StudyPostStatus.RECRUITING);

    for (StudyPost studyPost : studyPosts) {
      studyPost.setStatus(StudyPostStatus.CANCELED);
    }
  }

  // 모집 취소된 스터디 모집 기간 연장
  public void extendCanceledStudy(Long studyPostId, LocalDate newRecruitmentPeriod) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    validateStudyPostOwnership(studyPost.getUser().getId());

    if (!StudyPostStatus.CANCELED.equals(studyPost.getStatus())) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    if (newRecruitmentPeriod.isAfter(studyPost.getRecruitmentPeriod().plusMonths(1))) {
      throw new CustomException(ErrorCode.STUDY_EXTENSION_FAILED);
    }

    studyPost.setStatus(StudyPostStatus.RECRUITING);
    studyPost.setRecruitmentPeriod(newRecruitmentPeriod);
    studyPostRepository.save(studyPost);
  }

  // 모집글 작성자 검증
  private void validateStudyPostOwnership(Long studyPostOwnerId) {
    Long loggedInUserId = authService.getLoginUserId();
    if (!studyPostOwnerId.equals(loggedInUserId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }
  }

  // 생성 요청 사용자 검증
  private void validateUserRequestOwnership(Long requestUserId) {
    Long loggedInUserId = authService.getLoginUserId();
    System.out.println("requestUserId: " + requestUserId);
    System.out.println("loggedInUserId: " + loggedInUserId);
    if (!requestUserId.equals(loggedInUserId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }
  }

  // 스터디 모집글 엔티티 생성
  private StudyPost buildStudyPost(StudyPostCreateRequest request, User user) {
    int dayType = DayTypeUtils.encodeDaysFromRequest(request.getDayType());
    return StudyPost.builder()
        .title(request.getTitle())
        .studyName(request.getStudyName())
        .subject(request.getSubject())
        .difficulty(request.getDifficulty())
        .dayType(dayType)
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .startTime(request.getStartTime())
        .endTime(request.getEndTime())
        .meetingType(request.getMeetingType())
        .recruitmentPeriod(request.getRecruitmentPeriod())
        .description(request.getDescription())
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .address(request.getAddress())
        .status(StudyPostStatus.RECRUITING) // 기본값 설정
        .thumbnailImgUrl(request.getThumbnailImgUrl())
        .maxParticipants(request.getMaxParticipants())
        .currentParticipants(0) // 기본값: 0명
        .user(user)
        .build();
  }
}