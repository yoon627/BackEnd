package com.devonoff.domain.studyPost.service;

import static com.devonoff.type.ErrorCode.UNAUTHORIZED_ACCESS;
import static com.devonoff.type.ErrorCode.USER_NOT_FOUND;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.student.repository.StudentRepository;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.study.service.StudyService;
import com.devonoff.domain.studyPost.dto.StudyCommentDto;
import com.devonoff.domain.studyPost.dto.StudyCommentRequest;
import com.devonoff.domain.studyPost.dto.StudyCommentResponse;
import com.devonoff.domain.studyPost.dto.StudyPostCreateRequest;
import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.domain.studyPost.dto.StudyPostUpdateRequest;
import com.devonoff.domain.studyPost.dto.StudyReplyDto;
import com.devonoff.domain.studyPost.dto.StudyReplyRequest;
import com.devonoff.domain.studyPost.entity.StudyComment;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studyPost.entity.StudyReply;
import com.devonoff.domain.studyPost.repository.StudyCommentRepository;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.studyPost.repository.StudyReplyRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  private final StudyCommentRepository studyCommentRepository;
  private final StudyReplyRepository studyReplyRepository;

  @Value("${cloud.aws.s3.default-thumbnail-image-url}")
  private String defaultThumbnailImageUrl;

  // 생성
  @Transactional
  public StudyPostDto createStudyPost(StudyPostCreateRequest request) {
    User user = validateAndGetUser(request.getUserId());
    validateOwnership(request.getUserId());
    validateMaxParticipants(request.getMaxParticipants());
    validateMeetingType(request);

    String thumbnailImgUrl = handleFileUpload(request.getFile(), null);
    StudyPost studyPost = buildStudyPost(request, user, thumbnailImgUrl);

    studyPostRepository.save(studyPost);
    return StudyPostDto.fromEntity(studyPost);
  }

  // 상세 조회
  public StudyPostDto getStudyPostDetail(Long studyPostId) {
    return StudyPostDto.fromEntity(validateAndGetStudyPost(studyPostId));
  }

  // 상세 조회(userId)
  public Page<StudyPostDto> getStudyPostsByUserId(Long userId, Pageable pageable) {
    validateOwnership(userId);
    Page<StudyPost> studyPosts = studyPostRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

    return studyPosts.map(StudyPostDto::fromEntity);
  }

  // 조회 (검색리스트)
  public Page<StudyPostDto> searchStudyPosts(StudyMeetingType meetingType, String title,
      StudySubject subject, StudyDifficulty difficulty, int dayType, StudyPostStatus status,
      Double latitude, Double longitude, Pageable pageable) {

    return studyPostRepository.findStudyPostsByFilters(meetingType, title, subject, difficulty,
        dayType, status, latitude, longitude, pageable);
  }

  // 수정
  @Transactional
  public StudyPostDto updateStudyPost(Long studyPostId, StudyPostUpdateRequest request) {
    StudyPost studyPost = validateAndGetStudyPost(studyPostId);
    validateOwnership(studyPost.getUser().getId());
    validateMaxParticipants(request.getMaxParticipants());

    String updatedImgUrl = handleFileUpload(request.getFile(), studyPost.getThumbnailImgUrl());
    studyPost.updateFields(request, updatedImgUrl);

    studyPostRepository.save(studyPost);

    return StudyPostDto.fromEntity(studyPost);
  }

  // 모집 마감 및 스터디 진행 시작
  @Transactional
  public void closeStudyPost(Long studyPostId) {
    StudyPost studyPost = validateAndGetStudyPost(studyPostId);
    validateOwnership(studyPost.getUser().getId());

    if (studyPost.getStatus() != StudyPostStatus.RECRUITING) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    studyPost.closeRecruitment();

    List<StudySignup> approvedSignups = studySignupRepository.findByStudyPostAndStatus(
        studyPost, StudySignupStatus.APPROVED);

    if (approvedSignups.isEmpty()) {
      throw new CustomException(ErrorCode.NO_APPROVED_SIGNUPS);
    }

    Study study = studyService.createStudyFromClosedPost(studyPostId);
    studentRepository.saveAll(buildStudents(approvedSignups, study, studyPost.getUser()));
  }

  // 모집 취소 -> 사용자가 직접 취소
  public void cancelStudyPost(Long studyPostId) {
    StudyPost studyPost = validateAndGetStudyPost(studyPostId);
    validateOwnership(studyPost.getUser().getId());

    if (studyPost.getStatus() != StudyPostStatus.RECRUITING) {
      throw new CustomException(ErrorCode.INVALID_STUDY_STATUS);
    }

    studyPost.cancelRecruitment();
    studyPostRepository.save(studyPost);
  }

  // 모집 취소 -> 배치 작업으로 자동 취소
  @Transactional
  public void cancelStudyPostIfExpired() {
    LocalDate currentDate = LocalDate.now();

    List<StudyPost> expiredPosts = studyPostRepository.findAllByRecruitmentPeriodBeforeAndStatus(
        currentDate, StudyPostStatus.RECRUITING);

    expiredPosts.forEach(post -> {
      post.cancelRecruitment();
    });

    studyPostRepository.saveAll(expiredPosts);
  }

  // 모집 취소된 스터디 모집 기간 연장
  public void extendCanceledStudy(Long studyPostId, LocalDate newRecruitmentPeriod) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    validateOwnership(studyPost.getUser().getId());

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

  // 댓글

  /**
   * 댓글 작성
   *
   * @param studyPostId
   * @param studyCommentRequest
   * @return StudyCommentDto
   */
  public StudyCommentDto createStudyPostComment(
      Long studyPostId, StudyCommentRequest studyCommentRequest
  ) {
    Long loginUserId = authService.getLoginUserId();
    User user = userRepository.findById(loginUserId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    StudyComment savedStudyComment = studyCommentRepository.save(
        StudyCommentRequest.toEntity(user, studyPost, studyCommentRequest)
    );

    return StudyCommentDto.fromEntity(savedStudyComment);
  }

  /**
   * 댓글 조회
   *
   * @param studyPostId
   * @return Page<StudyCommentDto>
   */
  public Page<StudyCommentResponse> getStudyPostComments(Long studyPostId, Integer page) {
    StudyPost studyPost = studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));

    Pageable pageable = PageRequest.of(page, 12, Sort.by("createdAt").ascending());

    return studyCommentRepository.findAllByStudyPost(studyPost, pageable)
        .map(StudyCommentResponse::fromEntity);
  }

  /**
   * 댓글 수정
   *
   * @param commentId
   * @param studyCommentRequest
   * @return StudyCommentDto
   */
  public StudyCommentDto updateStudyPostComment(
      Long commentId, StudyCommentRequest studyCommentRequest
  ) {
    StudyComment studyComment = studyCommentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), studyComment.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    studyComment.setIsSecret(studyCommentRequest.getIsSecret());
    studyComment.setContent(studyCommentRequest.getContent());

    return StudyCommentDto.fromEntity(studyCommentRepository.save(studyComment));
  }

  /**
   * 댓글 삭제
   *
   * @param commentId
   * @return StudyCommentDto
   */
  @Transactional
  public StudyCommentDto deleteStudyPostComment(Long commentId) {
    StudyComment studyComment = studyCommentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), studyComment.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    studyReplyRepository.deleteAllByComment(studyComment);
    studyCommentRepository.delete(studyComment);

    return StudyCommentDto.fromEntity(studyComment);
  }

  // 대댓글

  /**
   * 대댓글 작성
   *
   * @param commentId
   * @param studyReplyRequest
   * @return StudyReplyDto
   */
  public StudyReplyDto createStudyPostReply(
      Long commentId, StudyReplyRequest studyReplyRequest
  ) {
    Long loginUserId = authService.getLoginUserId();
    User user = userRepository.findById(loginUserId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    StudyComment studyComment = studyCommentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    StudyReply savedStudyReply = studyReplyRepository.save(
        StudyReplyRequest.toEntity(user, studyComment, studyReplyRequest)
    );

    return StudyReplyDto.fromEntity(savedStudyReply);
  }

  /**
   * 대댓글 수정
   *
   * @param replyId
   * @param studyReplyRequest
   * @return StudyReplyDto
   */
  public StudyReplyDto updateStudyPostReply(
      Long replyId, StudyReplyRequest studyReplyRequest
  ) {
    StudyReply studyReply = studyReplyRepository.findById(replyId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), studyReply.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    studyReply.setIsSecret(studyReplyRequest.getIsSecret());
    studyReply.setContent(studyReplyRequest.getContent());

    return StudyReplyDto.fromEntity(studyReplyRepository.save(studyReply));
  }

  /**
   * 대댓글 삭제
   *
   * @param replyId
   * @return StudyReplyDto
   */
  public StudyReplyDto deleteStudyPostReply(Long replyId) {
    StudyReply studyReply = studyReplyRepository.findById(replyId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), studyReply.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    studyReplyRepository.delete(studyReply);

    return StudyReplyDto.fromEntity(studyReply);
  }

  // ================================= Helper methods ================================= //

  // 사용자 검증
  private User validateAndGetUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }

  // 모집글 검증
  private StudyPost validateAndGetStudyPost(Long studyPostId) {
    return studyPostRepository.findById(studyPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.STUDY_POST_NOT_FOUND));
  }

  // 로그인한 사용자와 소유자 일치 여부 검증
  private void validateOwnership(Long ownerId) {
    if (!ownerId.equals(authService.getLoginUserId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
    }
  }

  // 최대 참가자 수(2~10) 검증
  private void validateMaxParticipants(Integer maxParticipants) {
    if (maxParticipants < 2 || maxParticipants > 10) {
      throw new CustomException(ErrorCode.INVALID_MAX_PARTICIPANTS);
    }
  }

  // HYBRID 타입인 경우 위치 정보 존재 여부 검증
  private void validateMeetingType(StudyPostCreateRequest request) {
    if (request.getMeetingType() == StudyMeetingType.HYBRID &&
        (request.getLatitude() == null || request.getLongitude() == null
            || request.getAddress() == null)) {
      throw new CustomException(ErrorCode.LOCATION_REQUIRED_FOR_HYBRID);
    }
  }

  // 파일 존재 여부 검증
  private String handleFileUpload(MultipartFile file, String existingUrl) {
    if (file != null && !file.isEmpty()) {
      if (existingUrl != null) {
        photoService.delete(existingUrl);
      }
      return photoService.save(file);
    }
    return (existingUrl != null) ? existingUrl : defaultThumbnailImageUrl;
  }

  // 스터디 참가자 목록 생성
  private List<Student> buildStudents(List<StudySignup> signups, Study study, User leader) {
    List<Student> members = new ArrayList<>();
    members.add(Student.builder().study(study).user(leader).isLeader(true).build());
    signups.forEach(signup -> members.add(
        Student.builder().study(study).user(signup.getUser()).isLeader(false).build()));
    return members;
  }

  // 스터디 모집글 엔티티 생성
  private StudyPost buildStudyPost(
      StudyPostCreateRequest request, User user, String thumbnailImgUrl) {
    return StudyPost.builder()
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
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .address(request.getAddress())
        .status(StudyPostStatus.RECRUITING) // 기본값 설정
        .thumbnailImgUrl(thumbnailImgUrl)
        .maxParticipants(request.getMaxParticipants())
        .currentParticipants(0) // 기본값: 0명
        .user(user)
        .build();
  }
}