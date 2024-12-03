package com.devonoff.domain.comment.service;

import com.devonoff.domain.comment.dto.CommentDto;
import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.user.entity.User;
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

  @Transactional
  public CommentDto createComment(CommentDto dto, User user) {
    if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_COMMENT_CONTENT);
    } else if (dto.getContent().length() > 500) { // 500자는 예시로
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
    return toDto(savedComment);
  }

  @Transactional(readOnly = true)
  public List<CommentDto> getCommentsByPost(Long postId, PostType postType) {
    // 댓글 리스트를 가져오고 DTO로 변환하여 반환
    List<Comment> comments = commentRepository.findByPostIdAndPostType(postId, postType);
    if (comments.isEmpty()) {
      return List.of();
    }
      return comments.stream().map(this::toDto).toList();

  }

  @Transactional
  public CommentDto updateComment(Long commentId, String content, Boolean isSecret, User user) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    // 작성자 확인
    if (!comment.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
    }

    // 수정
    comment.setContent(content);
    comment.setIsSecret(isSecret);
    Comment updatedComment = commentRepository.save(comment);

    // 수정된 엔티티를 DTO로 반환
    return toDto(updatedComment);
  }

  @Transactional
  public void deleteComment(Long commentId, User user) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    // 작성자 확인
    if (!comment.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
    }

    commentRepository.delete(comment);
  }

  // Comment 엔티티를 DTO로 변환하는 유틸리티 메서드
  private CommentDto toDto(Comment comment) {
    return new CommentDto(
        comment.getId(),
        comment.getPostType(),
        comment.getPostId(),
        comment.getIsSecret(),
        comment.getContent(),
        comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null,
        comment.getUpdatedAt() != null ? comment.getUpdatedAt().toString() : null,
        comment.getUser() != null ? comment.getUser().getId() : null
    );
  }
}