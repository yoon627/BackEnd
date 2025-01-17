package com.devonoff.domain.infosharepost.service;

import static com.devonoff.type.ErrorCode.POST_NOT_FOUND;
import static com.devonoff.type.ErrorCode.UNAUTHORIZED_ACCESS;
import static com.devonoff.type.ErrorCode.USER_NOT_FOUND;

import com.devonoff.domain.infosharepost.dto.InfoShareCommentDto;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentRequest;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentResponse;
import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.dto.InfoShareReplyDto;
import com.devonoff.domain.infosharepost.dto.InfoShareReplyRequest;
import com.devonoff.domain.infosharepost.entity.InfoShareComment;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.entity.InfoShareReply;
import com.devonoff.domain.infosharepost.repository.InfoShareCommentRepository;
import com.devonoff.domain.infosharepost.repository.InfoSharePostRepository;
import com.devonoff.domain.infosharepost.repository.InfoShareReplyRepository;
import com.devonoff.domain.notification.dto.NotificationDto;
import com.devonoff.domain.notification.service.NotificationService;
import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.NotificationType;
import com.devonoff.type.PostType;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoSharePostService {

  private final InfoSharePostRepository infoSharePostRepository;
  private final UserRepository userRepository;
  private final InfoShareCommentRepository infoShareCommentRepository;
  private final InfoShareReplyRepository infoShareReplyRepository;
  private final PhotoService photoService;
  private final AuthService authService;
  private final NotificationService notificationService;

  @Value("${spring.data.web.pageable.default-page-size}")
  private Integer defaultPageSize;

  @Value("${cloud.aws.s3.default-thumbnail-image-url}")
  private String defaultThumbnailImageUrl;

  @Transactional
  public InfoSharePostDto createInfoSharePost(InfoSharePostDto infoSharePostDto) {
    User user = this.userRepository.findById(authService.getLoginUserId())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    infoSharePostDto.setUser(UserDto.fromEntity(user));
    MultipartFile file = infoSharePostDto.getFile();
    if (file != null && !file.isEmpty()) {
      infoSharePostDto.setThumbnailImgUrl(photoService.save(file));
    } else {
      infoSharePostDto.setThumbnailImgUrl(defaultThumbnailImageUrl);
    }

    return InfoSharePostDto.fromEntity(
        this.infoSharePostRepository.save(InfoSharePostDto.toEntity(infoSharePostDto)));
  }

  public Page<InfoSharePostDto> getInfoSharePosts(Integer page, String search) {
    Pageable pageable = PageRequest.of(page, defaultPageSize, Sort.by("createdAt").descending());
    return this.infoSharePostRepository.findAllByTitleContaining(search,
        pageable).map(InfoSharePostDto::fromEntity);
  }

  public Page<InfoSharePostDto> getInfoSharePostsByUserId(Long userId, Integer page,
      String search) {
    Pageable pageable = PageRequest.of(page, defaultPageSize, Sort.by("createdAt").descending());
    return this.infoSharePostRepository.findAllByUserIdAndTitleContaining(userId, search, pageable)
        .map(InfoSharePostDto::fromEntity);
  }

  public InfoSharePostDto getInfoSharePostByPostId(Long infoPostId) {
    return InfoSharePostDto.fromEntity(this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND)));
  }

  @Transactional
  public InfoSharePostDto updateInfoSharePost(Long infoPostId, InfoSharePostDto infoSharePostDto) {
    if (!Objects.equals(authService.getLoginUserId(), infoSharePostDto.getUserId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }
    InfoSharePost infoSharePost = this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    MultipartFile file = infoSharePostDto.getFile();
    String originImgUrl = infoSharePost.getThumbnailImgUrl();
    String requestImgUrl = infoSharePostDto.getThumbnailImgUrl();

    if (file != null && !file.isEmpty()) {
      infoSharePost.setThumbnailImgUrl(photoService.save(file));
      photoService.delete(originImgUrl);
    } else {
      if (requestImgUrl != null && !requestImgUrl.isEmpty() && requestImgUrl.equals(
          defaultThumbnailImageUrl)) {
        photoService.delete(originImgUrl);
        infoSharePost.setThumbnailImgUrl(defaultThumbnailImageUrl);
      }
    }

    infoSharePost.setTitle(infoSharePostDto.getTitle());
    infoSharePost.setDescription(infoSharePostDto.getDescription());
    return InfoSharePostDto.fromEntity(this.infoSharePostRepository.save(infoSharePost));
  }

  @Transactional
  public void deleteInfoSharePost(Long infoPostId) {
    InfoSharePost infoSharePost = this.infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    if (!Objects.equals(authService.getLoginUserId(), infoSharePost.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }
    photoService.delete(infoSharePost.getThumbnailImgUrl());
    List<InfoShareComment> commentList =
        infoShareCommentRepository.findAllByInfoSharePost(infoSharePost);

    for (InfoShareComment comment : commentList) {
      infoShareReplyRepository.deleteAllByComment(comment);
    }

    infoShareCommentRepository.deleteAllByInfoSharePost(infoSharePost);
    this.infoSharePostRepository.deleteById(infoPostId);
  }

  // 댓글

  /**
   * 댓글 생성
   *
   * @param infoPostId
   * @param infoShareCommentRequest
   * @return InfoSharePostCommentDto
   */
  public InfoShareCommentDto createInfoSharePostComment(
      Long infoPostId, InfoShareCommentRequest infoShareCommentRequest
  ) {
    Long loginUserId = authService.getLoginUserId();
    User user = userRepository.findById(loginUserId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    InfoSharePost infoSharePost = infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    InfoShareComment savedPostComment = infoShareCommentRepository.save(
        InfoShareCommentRequest.toEntity(user, infoSharePost, infoShareCommentRequest)
    );
    notificationService.sendNotificationToUser(infoSharePost.getUser().getId(),
        NotificationDto.builder()
            .type(NotificationType.COMMENT_ADDED)
            .userId(infoSharePost.getUser().getId())
            .sender(UserDto.fromEntity(user))
            .postType(PostType.INFO)
            .postTitle(infoSharePost.getTitle())
            .postContent(infoSharePost.getDescription())
            .commentContent(infoShareCommentRequest.getContent())
            .targetId(infoSharePost.getId())
            .isRead(false)
            .build());
    return InfoShareCommentDto.fromEntity(savedPostComment);
  }

  /**
   * 댓글 조회
   *
   * @param infoPostId
   * @return Page<InfoShareCommentResponse>
   */
  public Page<InfoShareCommentResponse> getInfoSharePostComments(Long infoPostId, Integer page) {
    InfoSharePost infoSharePost = infoSharePostRepository.findById(infoPostId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    Pageable pageable = PageRequest.of(page, 12, Sort.by("createdAt").ascending());

    return infoShareCommentRepository.findAllByInfoSharePost(infoSharePost, pageable)
        .map(InfoShareCommentResponse::fromEntity);
  }

  /**
   * 댓글 수정
   *
   * @param commentId
   * @param infoShareCommentRequest
   * @return InfoShareCommentDto
   */
  public InfoShareCommentDto updateInfoSharePostComment(
      Long commentId, InfoShareCommentRequest infoShareCommentRequest
  ) {
    InfoShareComment infoShareComment = infoShareCommentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), infoShareComment.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    infoShareComment.setIsSecret(infoShareCommentRequest.getIsSecret());
    infoShareComment.setContent(infoShareCommentRequest.getContent());

    return InfoShareCommentDto.fromEntity(
        infoShareCommentRepository.save(infoShareComment)
    );
  }

  /**
   * 댓글 삭제
   *
   * @param commentId
   * @return InfoShareCommentDto
   */
  @Transactional
  public InfoShareCommentDto deleteInfoSharePostComment(Long commentId) {
    InfoShareComment infoShareComment = infoShareCommentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), infoShareComment.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    infoShareReplyRepository.deleteAllByComment(infoShareComment);
    infoShareCommentRepository.delete(infoShareComment);

    return InfoShareCommentDto.fromEntity(infoShareComment);
  }

  // 대댓글

  /**
   * 대댓글 작성
   *
   * @param commentId
   * @param infoShareReplyRequest
   * @return InfoShareReplyDto
   */
  public InfoShareReplyDto createInfoSharePostReply(
      Long commentId, InfoShareReplyRequest infoShareReplyRequest
  ) {
    Long loginUserId = authService.getLoginUserId();
    User user = userRepository.findById(loginUserId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    InfoShareComment infoShareComment = infoShareCommentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    InfoShareReply infoShareReply = infoShareReplyRepository.save(
        InfoShareReplyRequest.toEntity(
            user, infoShareComment, infoShareReplyRequest
        )
    );
    notificationService.sendNotificationToUser(infoShareComment.getUser().getId(),
        NotificationDto.builder()
            .type(NotificationType.REPLY_ADDED)
            .userId(infoShareComment.getUser().getId())
            .sender(UserDto.fromEntity(user))
            .postType(PostType.INFO)
            .postTitle(infoShareComment.getInfoSharePost().getTitle())
            .commentContent(infoShareComment.getContent())
            .replyContent(infoShareReply.getContent())
            .targetId(infoShareComment.getInfoSharePost().getId())
            .isRead(false)
            .build());
    return InfoShareReplyDto.fromEntity(infoShareReply);
  }

  /**
   * 대댓글 수정
   *
   * @param replyId
   * @param infoShareReplyRequest
   * @return InfoShareReplyDto
   */
  public InfoShareReplyDto updateInfoSharePostReply(
      Long replyId, InfoShareReplyRequest infoShareReplyRequest
  ) {
    InfoShareReply infoShareReply = infoShareReplyRepository.findById(replyId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), infoShareReply.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    infoShareReply.setIsSecret(infoShareReplyRequest.getIsSecret());
    infoShareReply.setContent(infoShareReplyRequest.getContent());

    return InfoShareReplyDto.fromEntity(infoShareReplyRepository.save(infoShareReply));
  }

  /**
   * 대댓글 삭제
   *
   * @param replyId
   * @return InfoShareReplyDto
   */
  public InfoShareReplyDto deleteInfoSharePostReply(Long replyId) {
    InfoShareReply infoShareReply = infoShareReplyRepository.findById(replyId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    if (!Objects.equals(authService.getLoginUserId(), infoShareReply.getUser().getId())) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    infoShareReplyRepository.delete(infoShareReply);

    return InfoShareReplyDto.fromEntity(infoShareReply);
  }
}