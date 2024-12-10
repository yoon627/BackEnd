package com.devonoff.domain.comment.service;

import com.devonoff.domain.comment.dto.CommentRequest;
import com.devonoff.domain.comment.dto.CommentResponse;
import com.devonoff.domain.comment.dto.CommentUpdateRequest;
import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final PostValidationService postValidationService;
  private final AuthService authService;


  /**
   * 댓글 생성 메서드
   *
   * @param commentRequest 댓글 요청 DTO
   * @return 생성된 댓글 응답 DTO
   */
  @Transactional
  public CommentResponse createComment(CommentRequest commentRequest) {

    // 게시글 존재 여부 검증
    postValidationService.validatePostExists(commentRequest.getPostType(),
        commentRequest.getPostId());

    // 인증된 사용자 아이디 가져오기
    User user = getAuthenticatedUser();

    // Comment 엔티티 생성 및 저장
    Comment comment = commentRequest.toEntity(user);
    Comment savedComment = commentRepository.save(comment);

    // 저장된 엔티티를 CommentResponse로 변환하여 반환
    return CommentResponse.fromEntity(savedComment);
  }

  /**
   * 특정 게시글의 댓글 목록 조회
   *
   * @param postId   게시글 ID
   * @param postType 게시글 타입
   * @param pageable 페이징 정보
   * @return 페이징된 댓글 응답 DTO
   */

  @Transactional(readOnly = true)
  public Page<CommentResponse> getComments(Long postId, PostType postType, Pageable pageable) {
    // 댓글 페이징 조회
    return commentRepository.findByPostIdAndPostType(postId, postType, pageable)
        .map(CommentResponse::fromEntity);
  }

  /**
   * 댓글 수정 메서드
   *
   * @param commentId            수정할 댓글 ID
   * @param commentUpdateRequest 댓글 수정 요청 DTO
   */

  @Transactional
  public void updateComment(Long commentId, CommentUpdateRequest commentUpdateRequest) {

    // 현재인증된 사용자 조회
    User user = getAuthenticatedUser();

    // 댓글ID로 댓글 조회
    Comment comment = getCommentById(commentId);

    // 작성자 검증
    validateCommentOwner(comment, user);

    // 수정
    comment.setContent(commentUpdateRequest.getContent());
    comment.setIsSecret(commentUpdateRequest.getIsSecret());
    // 수정된 내용 저장
    commentRepository.save(comment);
  }

  @Transactional
  public void deleteComment(Long commentId) {

    // 현재 인증된 사용자 조회
    User user = getAuthenticatedUser();

    // 댓글 ID로 댓글조회
    Comment comment = getCommentById(commentId);

    // 댓글 사용자 검증
    validateCommentOwner(comment, user);

    // 댓글삭제
    commentRepository.delete(comment);

  }

  // Helper Methods


  /**
   * 현재 인증된 사용자 정보 조회
   *
   * @return 인증된 사용자 엔티티
   */
  private User getAuthenticatedUser() {
    // SecurityContext에서 사용자 ID 추출
    Long userId = authService.getLoginUserId();

    // 사용자 ID로 사용자 엔티티 조회
    return userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
  }

  /**
   * 댓글 ID로 댓글 조회
   *
   * @param commentId 댓글 ID
   * @return 댓글 엔티티
   */
  private Comment getCommentById(Long commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND, "댓글을 찾을 수 없습니다."));
  }

  /**
   * 댓글 작성자 검증
   *
   * @param comment 댓글 엔티티
   * @param user    현재 인증된 사용자
   */
  private void validateCommentOwner(Comment comment, User user) {
    if (!comment.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을수 없습니다.");
    }
  }

}

