package com.devonoff.domain.reply.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.reply.Repository.ReplyRepository;
import com.devonoff.domain.reply.dto.ReplyRequest;
import com.devonoff.domain.reply.dto.ReplyResponse;
import com.devonoff.domain.reply.entity.Reply;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class ReplyServiceTest {

  @InjectMocks
  private ReplyService replyService;

  @Mock
  private ReplyRepository replyRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private UserRepository userRepository;

  public ReplyServiceTest() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("대댓글 생성 성공")
  void testCreateReplySuccess() {
    // Given
    Long commentId = 1L; // 부모 댓글 ID
    Long userId = 2L; // 로그인된 사용자 ID

    ReplyRequest replyRequest = ReplyRequest.builder()
        .content("This is a reply") // 대댓글 내용
        .isSecret(false) // 비밀 댓글 여부
        .postId(1L) // 게시물 ID
        .postType("QNA") // 게시물 타입
        .build();

    // Mock 객체 생성 및 동작 정의
    Comment parentComment = mock(Comment.class); // 부모 댓글 Mock
    User loggedInUser = mock(User.class); // 로그인된 사용자 Mock
    Reply savedReply = mock(Reply.class); // 저장된 대댓글 Mock

    // Mock 동작 정의
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(parentComment)); // 부모 댓글 반환
    when(userRepository.findById(userId)).thenReturn(Optional.of(loggedInUser)); // 사용자 반환
    when(replyRepository.save(any(Reply.class))).thenReturn(savedReply); // 대댓글 저장 Mock

    // Mock 객체 동작 추가 설정
    when(loggedInUser.getId()).thenReturn(userId); // 사용자 ID 반환
    when(loggedInUser.getUsername()).thenReturn("testUser"); // 사용자 이름 반환
    when(parentComment.getId()).thenReturn(commentId); // 부모 댓글 ID 반환
    when(savedReply.getId()).thenReturn(1L); // 저장된 대댓글 ID 반환
    when(savedReply.getContent()).thenReturn(replyRequest.getContent()); // 저장된 대댓글 내용 반환
    when(savedReply.getUser()).thenReturn(loggedInUser); // 저장된 대댓글 작성자 반환

    // 보안 컨텍스트 설정
    mockSecurityContext(userId);

    // When
    ReplyResponse response = replyService.createReply(commentId, replyRequest); // 대댓글 생성 호출

    // Then
    assertNotNull(response); // 응답이 null이 아닌지 검증
    assertEquals("This is a reply", response.getContent()); // 응답 내용 검증
    verify(commentRepository, times(1)).findById(commentId); // 부모 댓글 조회 호출 검증
    verify(userRepository, times(1)).findById(userId); // 사용자 조회 호출 검증
    verify(replyRepository, times(1)).save(any(Reply.class)); // 대댓글 저장 호출 검증
  }

  @Test
  @DisplayName("대댓글 생성 실패 - 부모 댓글 없음")
  void testCreateReplyCommentNotFound() {
    // Given
    Long commentId = 1L;
    ReplyRequest replyRequest = ReplyRequest.builder()
        .content("This is a reply")
        .isSecret(false)
        .postId(1L)
        .postType("BOARD")
        .build();

    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        replyService.createReply(commentId, replyRequest)
    );

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    verify(commentRepository, times(1)).findById(commentId);
    verifyNoInteractions(replyRepository);
  }

  // 보안 컨텍스트 설정 메서드
  private void mockSecurityContext(Long userId) {
    Authentication authentication = mock(Authentication.class);
    // Principal을 문자열로 설정
    when(authentication.getPrincipal()).thenReturn(userId.toString());

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);
  }


  @Test
  @DisplayName("대댓글 수정 성공")
  void testUpdateReplySuccess() {
    // Given
    Long replyId = 1L;
    Long userId = 2L;

    ReplyRequest replyRequest = ReplyRequest.builder()
        .content("Updated content")
        .isSecret(true)
        .build();

    Reply existingReply = mock(Reply.class);
    User loggedInUser = mock(User.class);
    Reply savedReply = mock(Reply.class);

    // Mock 동작 설정
    when(replyRepository.findById(replyId)).thenReturn(Optional.of(existingReply));
    when(existingReply.getUser()).thenReturn(loggedInUser);
    when(loggedInUser.getId()).thenReturn(userId);
    when(replyRepository.save(any(Reply.class))).thenReturn(savedReply);

    // Mock savedReply 필드값 설정
    when(savedReply.getId()).thenReturn(replyId);
    when(savedReply.getContent()).thenReturn("Updated content");
    when(savedReply.getUser()).thenReturn(loggedInUser);

    mockSecurityContext(userId);

    // When
    ReplyResponse response = replyService.updateReply(replyId, replyRequest);

    // Then
    assertNotNull(response);
    assertEquals("Updated content", response.getContent());
    verify(replyRepository, times(1)).findById(replyId);
    verify(replyRepository, times(1)).save(existingReply);
  }

  @Test
  @DisplayName("대댓글 수정 실패 - 권한 없음")
  void testUpdateReplyUnauthorized() {
    // Given
    Long replyId = 1L;
    Long userId = 2L;

    ReplyRequest replyRequest = ReplyRequest.builder()
        .content("Updated content")
        .isSecret(true)
        .build();

    Reply existingReply = mock(Reply.class);
    User anotherUser = mock(User.class);

    when(replyRepository.findById(replyId)).thenReturn(Optional.of(existingReply));
    when(existingReply.getUser()).thenReturn(anotherUser);
    when(anotherUser.getId()).thenReturn(99L);

    mockSecurityContext(userId);

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        replyService.updateReply(replyId, replyRequest)
    );

    assertEquals(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS, exception.getErrorCode());
    verify(replyRepository, times(1)).findById(replyId);
    verify(replyRepository, never()).save(any());
  }

  @Test
  @DisplayName("대댓글 삭제 성공")
  void testDeleteReplySuccess() {
    // Given
    Long replyId = 1L;
    Long userId = 2L;

    Reply existingReply = mock(Reply.class);
    User loggedInUser = mock(User.class);

    when(replyRepository.findById(replyId)).thenReturn(Optional.of(existingReply));
    when(existingReply.getUser()).thenReturn(loggedInUser);
    when(loggedInUser.getId()).thenReturn(userId);

    mockSecurityContext(userId);

    // When
    replyService.deleteReply(replyId);

    // Then
    verify(replyRepository, times(1)).findById(replyId);
    verify(replyRepository, times(1)).delete(existingReply);
  }

  @Test
  @DisplayName("대댓글 삭제 실패 - 권한 없음")
  void testDeleteReplyUnauthorized() {
    // Given
    Long replyId = 1L;
    Long userId = 2L;

    Reply existingReply = mock(Reply.class);
    User anotherUser = mock(User.class);

    when(replyRepository.findById(replyId)).thenReturn(Optional.of(existingReply));
    when(existingReply.getUser()).thenReturn(anotherUser);
    when(anotherUser.getId()).thenReturn(99L);

    mockSecurityContext(userId);

    // When & Then
    CustomException exception = assertThrows(CustomException.class, () ->
        replyService.deleteReply(replyId)
    );

    assertEquals(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS, exception.getErrorCode());
    verify(replyRepository, times(1)).findById(replyId);
    verify(replyRepository, never()).delete(any());
  }
}