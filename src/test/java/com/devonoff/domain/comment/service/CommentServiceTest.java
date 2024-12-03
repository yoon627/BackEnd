package com.devonoff.domain.comment.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.comment.dto.CommentDto;
import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CommentServiceTest {

  @InjectMocks
  private CommentService commentService;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private AuthService authService;

  public CommentServiceTest() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("댓글 생성 - 성공")
  void createComment_Success() {
    // Given
    Long userId = 1L;
    User user = User.builder().id(userId).build();
    CommentDto dto = CommentDto.builder()
        .postType(PostType.QNA_POST)
        .postId(100L)
        .content("This is a comment")
        .isSecret(false)
        .build();

    when(authService.findUserById(userId)).thenReturn(user);
    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    CommentDto result = commentService.createComment(dto, userId);

    // Then
    assertNotNull(result);
    assertEquals(dto.getContent(), result.getContent());
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 생성 - 실패 (글자 수 초과)")
  void createComment_Fail_ContentTooLong() {
    // Given
    Long userId = 1L;
    CommentDto dto = CommentDto.builder()
        .postType(PostType.QNA_POST)
        .postId(100L)
        .content("A".repeat(501))
        .isSecret(false)
        .build();

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () -> commentService.createComment(dto, userId));
    assertEquals(ErrorCode.INVALID_COMMENT_CONTENT, exception.getErrorCode());
  }

  @Test
  @DisplayName("댓글 조회 - 성공")
  void getCommentsByPost_Success() {
    // Given
    Long postId = 100L;
    PostType postType = PostType.QNA_POST;
    Comment comment = Comment.builder().id(1L).postId(postId).postType(postType).build();

    when(commentRepository.findByPostIdAndPostType(postId, postType)).thenReturn(List.of(comment));

    // When
    List<CommentDto> result = commentService.getCommentsByPost(postId, postType);

    // Then
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    verify(commentRepository, times(1)).findByPostIdAndPostType(postId, postType);
  }

  @Test
  @DisplayName("댓글 수정 - 성공")
  void updateComment_Success() {
    // Given
    Long commentId = 1L;
    Long userId = 1L;
    String newContent = "Updated content";
    Boolean isSecret = true;
    User user = User.builder().id(userId).build();
    Comment comment = Comment.builder().id(commentId).user(user).content("Old content").isSecret(false).build();

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0)); // save 호출 후 반환값 설정

    // When
    CommentDto result = commentService.updateComment(commentId, newContent, isSecret, userId);

    // Then
    assertNotNull(result);
    assertEquals(newContent, result.getContent());
    assertEquals(isSecret, result.getIsSecret());
    verify(commentRepository, times(1)).save(comment);
  }

  @Test
  @DisplayName("댓글 수정 - 실패 (권한 없음)")
  void updateComment_Fail_Unauthorized() {
    // Given
    Long commentId = 1L;
    Long userId = 1L;
    String newContent = "Updated content";
    Boolean isSecret = true;
    User user = User.builder().id(2L).build(); // 다른 사용자 ID
    Comment comment = Comment.builder().id(commentId).user(user).build();

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        commentService.updateComment(commentId, newContent, isSecret, userId));
    assertEquals(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS, exception.getErrorCode());
  }

  @Test
  @DisplayName("댓글 삭제 - 성공")
  void deleteComment_Success() {
    // Given
    Long commentId = 1L;
    Long userId = 1L;
    User user = User.builder().id(userId).build();
    Comment comment = Comment.builder().id(commentId).user(user).build();

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    // When
    assertDoesNotThrow(() -> commentService.deleteComment(commentId, userId));

    // Then
    verify(commentRepository, times(1)).delete(comment);
  }

  @Test
  @DisplayName("댓글 삭제 - 실패 (댓글 없음)")
  void deleteComment_Fail_NotFound() {
    // Given
    Long commentId = 1L;
    Long userId = 1L;

    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        commentService.deleteComment(commentId, userId));
    assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("댓글 삭제 - 실패 (권한 없음)")
  void deleteComment_Fail_Unauthorized() {
    // Given
    Long commentId = 1L;
    Long userId = 1L;
    User user = User.builder().id(2L).build(); // 다른 사용자 ID
    Comment comment = Comment.builder().id(commentId).user(user).build();

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        commentService.deleteComment(commentId, userId));
    assertEquals(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS, exception.getErrorCode());
  }
}