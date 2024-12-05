package com.devonoff.domain.comment.service;

import com.devonoff.domain.comment.dto.CommentRequest;
import com.devonoff.domain.comment.dto.CommentResponse;
import com.devonoff.domain.comment.dto.CommentUpdateRequest;
import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  // 시큐리티에서 로그인된 사용자 유저아이디 꺼내기
  private Long extractUserIdFromPrincipal() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof String) {
      return Long.parseLong((String) principal);
    } else if (principal instanceof UserDetails) {
      return Long.parseLong(((UserDetails) principal).getUsername());
    } else {
      throw new CustomException(ErrorCode.USER_NOT_FOUND, "로그인된 사용자만 접근 가능합니다.");
    }
  }

  @Transactional
  public CommentResponse createComment(CommentRequest commentRequest) {

    // 인증된 사용자 아이디 가져오기
    Long userId = extractUserIdFromPrincipal();

    // 사용자 정보 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // Comment 엔티티 생성 및 저장
    Comment comment = commentRequest.toEntity(user);
    Comment savedComment = commentRepository.save(comment);

    // 저장된 엔티티를 CommentResponse로 변환하여 반환
    return CommentResponse.fromEntity(savedComment);
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getComments(Long postId, PostType postType, Pageable pageable) {

    // 댓글 페이징 조회
    Page<Comment> comments = commentRepository.findByPostIdAndPostType(postId, postType, pageable);

    // Comment -> CommentResponse 변환
    return comments.map(CommentResponse::fromEntity);
  }


  @Transactional
  public void updateComment(Long commentId, CommentUpdateRequest commentUpdateRequest) {

    Long userId = extractUserIdFromPrincipal();

    // 사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 댓글 조회
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND, "댓글을 찾을 수 없습니다."));

    // 작성자 확인
    if (!comment.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
    }

    // 수정
    comment.setContent(commentUpdateRequest.getContent());
    comment.setIsSecret(commentUpdateRequest.getIsSecret());
    commentRepository.save(comment);
  }

  @Transactional
  public void deleteComment(Long commentId) {
    Long userId = extractUserIdFromPrincipal();

    // 사용자 정보 확인
    User user = userRepository.findById(Long.parseLong(String.valueOf(userId)))
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 댓글 조회
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND, "댓글을 찾을 수 없습니다."));

    // 작성자 확인
    if (!comment.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS, "삭제 권한이 없습니다.");
    }

    // 댓글 삭제
    commentRepository.delete(comment);
  }


}