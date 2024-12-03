package com.devonoff.domain.comment.service;

import com.devonoff.domain.comment.dto.CommentDto;
import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final AuthService authService;
  // 댓글의 최대 글자수
  private static final int MAX_COMMENT_LENGTH = 500;

  @Transactional
  public CommentDto createComment(CommentDto dto, Long userId) {
    log.info("Creating comment for user: {}, postId: {}", userId, dto.getPostId());
    // User 객체 가져오기
    User user = authService.findUserById(userId);

    if (dto.getContent().length() > MAX_COMMENT_LENGTH) {
      throw new CustomException(ErrorCode.INVALID_COMMENT_CONTENT);
    }
    Comment comment = Comment.builder()
        .postType(dto.getPostType())
        .postId(dto.getPostId())
        .isSecret(dto.getIsSecret())
        .content(dto.getContent())
        .user(user) // 로그인된 사용자 연결
        .build();

    Comment savedComment = commentRepository.save(comment);

    // 저장된 엔티티를 DTO로 변환하여 반환
    return CommentDto.fromEntity(savedComment);
  }

  @Transactional(readOnly = true)
  public List<CommentDto> getCommentsByPost(Long postId, PostType postType) {
    // 댓글 리스트를 가져오고 DTO로 변환하여 반환
    List<Comment> comments = commentRepository.findByPostIdAndPostType(postId, postType);
    if (comments.isEmpty()) {
      return List.of();
    }
    return comments.stream().map(CommentDto::fromEntity).toList();

  }

  @Transactional
  public CommentDto updateComment(Long commentId, String content, Boolean isSecret, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    // 작성자 확인
    if (!comment.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
    }

    // 수정
    comment.setContent(content);
    comment.setIsSecret(isSecret);
    Comment updatedComment = commentRepository.save(comment);

    // 수정된 엔티티를 DTO로 반환
    return CommentDto.fromEntity(updatedComment);
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    // 작성자 확인
    if (!comment.getUser().getId().equals(userId)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
    }

    commentRepository.delete(comment);
  }


}