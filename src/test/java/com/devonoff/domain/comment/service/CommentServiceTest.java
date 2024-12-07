package com.devonoff.domain.comment.service;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.comment.dto.CommentRequest;
import com.devonoff.domain.comment.dto.CommentResponse;
import com.devonoff.domain.comment.dto.CommentUpdateRequest;
import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.comment.repository.CommentRepository;
import com.devonoff.domain.qnapost.entity.QnaPost;
import com.devonoff.domain.qnapost.repository.QnaPostRepository;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @InjectMocks
  private CommentService commentService;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private StudyPostRepository studyPostRepository;

  @Mock
  private QnaPostRepository qnaPostRepository;


  @BeforeEach
  void setup() {
    Map<PostType, Object> repositoryMap = new HashMap<>();
    repositoryMap.put(PostType.STUDY, studyPostRepository);
    repositoryMap.put(PostType.QNA, qnaPostRepository);
    ReflectionTestUtils.setField(commentService, "repositoryMap", repositoryMap);


    // SecurityContext 설정
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);

    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    lenient().when(authentication.getPrincipal()).thenReturn("1");
   // when(securityContext.getAuthentication()).thenReturn(authentication);
   // when(authentication.getPrincipal()).thenReturn("1"); // 사용자 ID를 String으로 설정

    SecurityContextHolder.setContext(securityContext);
  }


  @Test
  @DisplayName("댓글 생성 - 성공 (QnA 게시글)")
  void testCreateComment_Success_Qna() {
    // Mock 데이터 생성
    QnaPost qnaPost = new QnaPost(); // QnaPost 객체 생성
    User user = User.builder()
        .id(1L)
        .nickname("user1")
        .email("test@test.com")
        .password("password")
        .build();

    CommentRequest commentRequest = CommentRequest.builder()
        .postId(1L)
        .postType(PostType.QNA)
        .content("댓글 내용")
        .isSecret(false)
        .build();

    Comment comment = Comment.builder()
        .id(1L)
        .postId(1L)
        .postType(PostType.QNA)
        .content("댓글 내용")
        .isSecret(false)
        .user(user)
        .build();

    // Mocking
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(qnaPost)); // QnaPost 반환
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(commentRepository.save(any(Comment.class))).thenReturn(comment);

    // 실행
    CommentResponse response = commentService.createComment(commentRequest);

    // 검증
    assertNotNull(response);
    assertEquals("댓글 내용", response.getContent());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 생성 - 실패: 사용자 정보 없음")
  void testCreateComment_Fail_UserNotFound() {
    // given
    CommentRequest commentRequest = CommentRequest.builder()
        .postId(1L)
        .postType(PostType.QNA)
        .content("댓글 내용")
        .isSecret(false)
        .build();

    // 게시글이 존재한다고 가정(Mock 설정)
    QnaPost mockQnaPost = QnaPost.builder()
        .id(1L)
        .title("Test QnA Post")
        .build(); // QnaPost Mock 생성

    // Mocking 정확히 수정
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(mockQnaPost));

    // 사용자가 없다고 설정(Mock)
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        commentService.createComment(commentRequest));

    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("동시 조회 부하 테스트 - 동시성문제")
  void testConcurrentGetCommentsImproved() throws InterruptedException {
    Long postId = 1L;
    PostType postType = PostType.QNA;
    Pageable pageable = PageRequest.of(0, 10);

    Comment comment = Comment.builder()
        .id(1L)
        .content("Sample Comment")
        .user(User.builder().id(1L).nickname("TestUser").build())
        .postId(postId)
        .postType(postType)
        .build();

    Page<Comment> mockPage = new PageImpl<>(List.of(comment));
    when(commentRepository.findByPostIdAndPostType(postId, postType, pageable)).thenReturn(mockPage);

    int threadCount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    List<CompletableFuture<Page<CommentResponse>>> futures = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      futures.add(CompletableFuture.supplyAsync(() -> {
        try {
          return commentService.getComments(postId, postType, pageable);
        } finally {
          latch.countDown();
        }
      }, executorService));
    }

    latch.await();

    executorService.shutdown();
    assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));

    for (CompletableFuture<Page<CommentResponse>> future : futures) {
      assertDoesNotThrow(() -> {
        Page<CommentResponse> responses = future
            .exceptionally(ex -> {
              fail("예외 발생: " + ex.getMessage());
              return null;
            })
            .get();
        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
        assertEquals("Sample Comment", responses.getContent().get(0).getContent());
      });
    }
  }
  @Test
  @DisplayName("댓글 수정 - 성공")
  void testUpdateComment_Success() {
    Long commentId = 1L;
    Long userId = 1L;

    User user = User.builder()
        .id(userId)
        .nickname("user1")
        .email("test@test.com")
        .build();

    Comment comment = Comment.builder()
        .id(commentId)
        .content("기존 내용")
        .isSecret(false)
        .user(user)
        .build();

    CommentUpdateRequest updateRequest = CommentUpdateRequest.builder()
        .content("수정된 내용")
        .isSecret(true)
        .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    commentService.updateComment(commentId, updateRequest);

    assertEquals("수정된 내용", comment.getContent());
    assertTrue(comment.getIsSecret());
    verify(commentRepository).save(comment);
  }

  @Test
  @DisplayName("댓글 삭제 - 성공")
  void testDeleteComment_Success() {
    Long commentId = 1L;
    Long userId = 1L;

    User user = User.builder()
        .id(userId)
        .nickname("user1")
        .email("test@test.com")
        .build();

    Comment comment = Comment.builder()
        .id(commentId)
        .content("댓글 내용")
        .isSecret(false)
        .user(user)
        .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    commentService.deleteComment(commentId);

    verify(commentRepository).delete(comment);
  }
}