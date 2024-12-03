package com.devonoff.domain.comment.service;

import com.devonoff.domain.comment.dto.CommentDto;
import com.devonoff.domain.comment.dto.CommentRequest;
import com.devonoff.domain.comment.dto.CommentResponse;
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
  public CommentResponse createComment(CommentRequest request, Long userId) {
    // 유저 정보 가져오기
    User user = authService.findUserById(userId);

    // 요청 값 검증
    validateRequest(request);

    // DTO -> Entity 변환
    Comment comment = request.toEntity(user);

    // 댓글 저장
    Comment savedComment = commentRepository.save(comment);

    // 로깅
    log.info("Comment created by user ID: {}, Post ID: {}, Comment ID: {}",
        userId, savedComment.getPostId(), savedComment.getId());

    // 저장된 엔티티를 응답 DTO로 변환
    return CommentResponse.fromEntity(savedComment);
  }

  private void validateRequest(CommentRequest request) {
    if (request.getContent() == null || request.getContent().isBlank()) {
      throw new CustomException(ErrorCode.INVALID_COMMENT_CONTENT, "댓글 내용을 입력하세요.");
    }
    if (request.getContent().length() > MAX_COMMENT_LENGTH) {
      throw new CustomException(ErrorCode.INVALID_COMMENT_CONTENT,
          "댓글 내용은 최대 " + MAX_COMMENT_LENGTH + "자까지 입력할 수 있습니다.");
    }
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