package com.devonoff.domain.qnapost.service;

import static com.devonoff.type.ErrorCode.UNAUTHORIZED_ACCESS;
import static com.devonoff.type.ErrorCode.USER_NOT_FOUND;

import com.devonoff.domain.notification.dto.NotificationDto;
import com.devonoff.domain.notification.service.NotificationService;
import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.qnapost.dto.QnaCommentDto;
import com.devonoff.domain.qnapost.dto.QnaCommentRequest;
import com.devonoff.domain.qnapost.dto.QnaCommentResponse;
import com.devonoff.domain.qnapost.dto.QnaPostDto;
import com.devonoff.domain.qnapost.dto.QnaPostRequest;
import com.devonoff.domain.qnapost.dto.QnaPostUpdateDto;
import com.devonoff.domain.qnapost.dto.QnaReplyDto;
import com.devonoff.domain.qnapost.dto.QnaReplyRequest;
import com.devonoff.domain.qnapost.entity.QnaComment;
import com.devonoff.domain.qnapost.entity.QnaPost;
import com.devonoff.domain.qnapost.entity.QnaReply;
import com.devonoff.domain.qnapost.repository.QnaCommentRepository;
import com.devonoff.domain.qnapost.repository.QnaPostRepository;
import com.devonoff.domain.qnapost.repository.QnaReplyRepository;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.NotificationType;
import com.devonoff.type.PostType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QnaPostService {

  private final QnaPostRepository qnaPostRepository;
  private final UserRepository userRepository;
  private final QnaCommentRepository qnaCommentRepository;
  private final QnaReplyRepository qnaReplyRepository;
  private final PhotoService photoService;
  private final AuthService authService;
  private final NotificationService notificationService;
  @Value("${cloud.aws.s3.default-thumbnail-image-url}")
  private String defaultThumbnailImageUrl;

  /**
   * 질의 응답 게시글 생성
   *
   * @param email
   * @param qnaPostRequest
   */
  @Transactional
  public ResponseEntity<Void> createQnaPost(QnaPostRequest qnaPostRequest, String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 썸네일 저장
    String uploadedThumbnailUrl = defaultThumbnailImageUrl;
    if (qnaPostRequest.getFile() != null && !qnaPostRequest.getFile().isEmpty()) {
      uploadedThumbnailUrl = photoService.save(qnaPostRequest.getFile());
    }

    // QnaPost 저장
    QnaPost qnaPost = QnaPost.builder()
        .title(qnaPostRequest.getTitle())
        .content(qnaPostRequest.getContent())
        .thumbnailUrl(uploadedThumbnailUrl)
        .user(user)
        .build();

    qnaPostRepository.save(qnaPost);

    // 상태 코드만 반환
    return ResponseEntity.ok().build(); // HTTP 200
  }

  /**
   * 질의 응답 게시글 전체 목록 조회 (최신순) 토큰 X
   *
   * @param pageable 조회할 페이지 번호 (1부터 시작)
   * @param search   검색 키워드 (optional)
   * @return Page<QnaPostDto>
   */
  public Page<QnaPostDto> getQnaPostList(Pageable pageable, String search) {

    // 검색 조건에 따라 전체 게시물 또는 검색 결과 반환
    if (search == null || search.isBlank() || search.equals("")) {
      return qnaPostRepository.findAllByOrderByCreatedAtDesc(pageable)
          .map(QnaPostDto::fromEntity);
    }

    return qnaPostRepository.findByTitleContainingOrderByCreatedAtDesc(search.trim(), pageable)
        .map(QnaPostDto::fromEntity);
  }

  /**
   * 특정 사용자의 질의 응답 게시글 목록 조회 (최신순) 토큰O
   *
   * @param userId
   * @param pageable
   * @param search
   * @return Page<QnaPostDto>
   */
  public Page<QnaPostDto> getQnaPostByUserIdList(Long userId, Pageable pageable, String search) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 검색어가 없을 경우와 있을 경우 구분
    Page<QnaPost> posts = (search != null && !search.isBlank())
        ? qnaPostRepository.findByUserAndTitleContainingOrderByCreatedAtDesc(user, search, pageable)
        : qnaPostRepository.findByUserOrderByCreatedAtDesc(user, pageable);

    // posts가 null인 경우 처리
    if (posts == null) {
      posts = new PageImpl<>(Collections.emptyList());
    }

    return posts.map(QnaPostDto::fromEntity);
  }

  /**
   * 특정 질의 응답 게시글 상세 조회 토큰 X
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  public QnaPostDto getQnaPost(Long qnaPostId) {
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    return QnaPostDto.fromEntity(qnaPost);
  }

  /**
   * 특정 질의 응답 게시글 수정
   *
   * @param qnaPostId
   * @param qnaPostUpdateDto
   * @return QnaPostDto
   */
  @Transactional
  public QnaPostDto updateQnaPost(Long qnaPostId,
      QnaPostUpdateDto qnaPostUpdateDto) {

    // 작성자 이메일을 DTO에서 가져오기
    String email = qnaPostUpdateDto.getAuthor();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 게시글 가져오기 및 작성자 확인
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    if (!qnaPost.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "작성자만 게시글을 수정할 수 있습니다.");
    }

    // 썸네일 업데이트 로직
    String updatedThumbnailUrl = qnaPostUpdateDto.getThumbnailImgUrl();
    if (qnaPostUpdateDto.getFile() != null && !qnaPostUpdateDto.getFile().isEmpty()) {
      updatedThumbnailUrl = photoService.save(qnaPostUpdateDto.getFile());
      qnaPost.setThumbnailUrl(updatedThumbnailUrl);
      photoService.delete(qnaPost.getThumbnailUrl());
    } else {
      if (updatedThumbnailUrl != null && !updatedThumbnailUrl.isEmpty()
          && updatedThumbnailUrl.equals(defaultThumbnailImageUrl)) {
        photoService.delete(qnaPost.getThumbnailUrl());
        qnaPost.setThumbnailUrl(defaultThumbnailImageUrl);
      }
    }

    // 게시글 업데이트
    qnaPost.setTitle(qnaPostUpdateDto.getTitle());
    qnaPost.setContent(qnaPostUpdateDto.getContent());
    QnaPost updatedQnaPost = qnaPostRepository.save(qnaPost);

    return QnaPostDto.fromEntity(updatedQnaPost);
  }

  /**
   * 특정 질의 응답 게시글 삭제
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  @Transactional
  public void deleteQnaPost(Long qnaPostId) {
    // SecurityContext에서 인증된 사용자 ID 가져오기
    Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

    // 사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 게시글 조회 및 작성자 확인
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    if (!qnaPost.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "작성자만 게시글을 삭제할 수 있습니다.");
    }

    // 썸네일 파일 삭제
    photoService.delete(qnaPost.getThumbnailUrl());

    // 관련된 댓글, 대댓글 삭제
    List<QnaComment> commentList = qnaCommentRepository.findAllByQnaPost(qnaPost);

    for (QnaComment comment : commentList) {
      qnaReplyRepository.deleteAllByComment(comment);
    }

    qnaCommentRepository.deleteAllByQnaPost(qnaPost);

    // 게시글 삭제
    qnaPostRepository.delete(qnaPost);
  }

  // 댓글


  /**
   * 댓글 작성
   *
   * @param qnaPostId
   * @param qnaCommentRequest
   * @return QnaCommentDto
   */
  public QnaCommentDto createQnaPostComment(
      Long qnaPostId, QnaCommentRequest qnaCommentRequest
  ) {
    Long loginUserId = authService.getLoginUserId();
    User user = userRepository.findById(loginUserId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    QnaComment savedQnaComment = qnaCommentRepository.save(
        QnaCommentRequest.toEntity(user, qnaPost, qnaCommentRequest)
    );
    notificationService.sendNotificationToUser(qnaPost.getUser().getId(),
        NotificationDto.builder()
            .type(NotificationType.COMMENT_ADDED)
            .userId(qnaPost.getUser().getId())
            .sender(UserDto.fromEntity(user))
            .postType(PostType.QNA)
            .postTitle(qnaPost.getTitle())
            .postContent(qnaPost.getContent())
            .commentContent(savedQnaComment.getContent())
            .targetId(qnaPost.getId())
            .isRead(false)
            .build()
    );
    return QnaCommentDto.fromEntity(savedQnaComment);
  }

  /**
   * 댓글 조회
   *
   * @param qnaPostId
   * @param page
   * @return Page<QnaCommentResponse>
   */
  public Page<QnaCommentResponse> getQnaPostComments(Long qnaPostId, Integer page) {
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    Pageable pageable = PageRequest.of(page, 12, Sort.by("createdAt").ascending());

    return qnaCommentRepository.findAllByQnaPost(qnaPost, pageable)
        .map(QnaCommentResponse::fromEntity);
  }

  /**
   * 댓글 수정
   *
   * @param commentId
   * @param qnaCommentRequest
   * @return QnaCommentDto
   */
  public QnaCommentDto updateQnaPostComment(
      Long commentId, QnaCommentRequest qnaCommentRequest
  ) {
    QnaComment qnaComment = qnaCommentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), qnaComment.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    qnaComment.setIsSecret(qnaCommentRequest.getIsSecret());
    qnaComment.setContent(qnaCommentRequest.getContent());

    return QnaCommentDto.fromEntity(qnaCommentRepository.save(qnaComment));
  }

  /**
   * 댓글 삭제
   *
   * @param commentId
   * @return QnaCommentDto
   */
  @Transactional
  public QnaCommentDto deleteQnaPostComment(Long commentId) {
    QnaComment qnaComment = qnaCommentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), qnaComment.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    qnaReplyRepository.deleteAllByComment(qnaComment);
    qnaCommentRepository.delete(qnaComment);

    return QnaCommentDto.fromEntity(qnaComment);
  }

  // 대댓글

  /**
   * 대댓글 작성
   *
   * @param commentId
   * @param qnaReplyRequest
   * @return QnaReplyDto
   */
  public QnaReplyDto createQnaPostReply(
      Long commentId, QnaReplyRequest qnaReplyRequest
  ) {
    Long loginUserId = authService.getLoginUserId();
    User user = userRepository.findById(loginUserId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    QnaComment qnaComment = qnaCommentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    QnaReply savedQnaReply = qnaReplyRepository.save(
        QnaReplyRequest.toEntity(user, qnaComment, qnaReplyRequest)
    );
    notificationService.sendNotificationToUser(qnaComment.getUser().getId(),
        NotificationDto.builder()
            .type(NotificationType.REPLY_ADDED)
            .userId(qnaComment.getUser().getId())
            .sender(UserDto.fromEntity(user))
            .postType(PostType.QNA)
            .postTitle(qnaComment.getQnaPost().getTitle())
            .commentContent(qnaComment.getContent())
            .replyContent(qnaReplyRequest.getContent())
            .targetId(qnaComment.getQnaPost().getId())
            .isRead(false)
            .build());
    return QnaReplyDto.fromEntity(savedQnaReply);
  }

  /**
   * 대댓글 수정
   *
   * @param replyId
   * @param qnaReplyRequest
   * @return QnaReplyDto
   */
  public QnaReplyDto updateQnaPostReply(
      Long replyId, QnaReplyRequest qnaReplyRequest
  ) {
    QnaReply qnaReply = qnaReplyRepository.findById(replyId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), qnaReply.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    qnaReply.setIsSecret(qnaReplyRequest.getIsSecret());
    qnaReply.setContent(qnaReplyRequest.getContent());

    return QnaReplyDto.fromEntity(qnaReplyRepository.save(qnaReply));
  }

  /**
   * 대댓글 삭제
   *
   * @param replyId
   * @return QnaReplyDto
   */
  public QnaReplyDto deleteQnaPostReply(Long replyId) {
    QnaReply qnaReply = qnaReplyRepository.findById(replyId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), qnaReply.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    qnaReplyRepository.delete(qnaReply);

    return QnaReplyDto.fromEntity(qnaReply);
  }
}