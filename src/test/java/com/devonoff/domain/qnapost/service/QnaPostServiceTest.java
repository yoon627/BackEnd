package com.devonoff.domain.qnapost.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

class QnaPostServiceTest {

  @Mock
  private QnaPostRepository qnaPostRepository;
  
  @Mock
  private QnaCommentRepository qnaCommentRepository;

  @Mock
  private QnaReplyRepository qnaReplyRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PhotoService photoService;
  
  @Mock
  private AuthService authService;

  @InjectMocks
  private QnaPostService qnaPostService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  // =======================================================================
  // createQnaPost 테스트
  // =======================================================================
  @DisplayName("QnaPostDto 생성 테스트")
  @Test
  void testQnaPostDtoCreation() {
    // Given
    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setNickname("testUser");

    QnaPost qnaPost = QnaPost.builder()
        .id(1L)
        .user(user)
        .title("Test Title")
        .content("Test Content")
        .build();

    // When
    QnaPostDto qnaPostDto = QnaPostDto.fromEntity(qnaPost);

    // Then
    assertThat(qnaPostDto.getId()).isEqualTo(1L);
    assertThat(qnaPostDto.getUser().getId()).isEqualTo(1L);
    assertThat(qnaPostDto.getTitle()).isEqualTo("Test Title");
    assertThat(qnaPostDto.getContent()).isEqualTo("Test Content");
  }

  @DisplayName("createQnaPost 실패 - 사용자 없음")
  @Test
  void createQnaPost_Failure_UserNotFound() {
    // Given
    String email = "nonexistent@example.com";
    QnaPostRequest request = new QnaPostRequest();

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // When
    CustomException exception = assertThrows(CustomException.class, () ->
        qnaPostService.createQnaPost(request, email)
    );

    // Then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    verify(qnaPostRepository, never()).save(any());
  }

  // =======================================================================
  // getQnaPostList 테스트
  // =======================================================================
  @DisplayName("getQnaPostList 성공 - 검색어 없이 전체 조회")
  @Test
  void getQnaPostList_Success_NoSearch() {
    // Given
    Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<QnaPost> emptyPage = new PageImpl<>(Collections.emptyList()); // 빈 페이지 객체 생성

    when(qnaPostRepository.findAll(pageable)).thenReturn(emptyPage);

    // When
    Page<QnaPostDto> result = qnaPostService.getQnaPostList(pageable, "");

    // Then
    assertThat(result.getContent()).isEmpty(); // 결과가 빈 리스트인지 확인
    verify(qnaPostRepository, times(1)).findAll(pageable);
  }



  // =======================================================================
  // getQnaPostByUserIdList 테스트
  // =======================================================================
  @DisplayName("getQnaPostByUserIdList 성공 - 사용자 ID로 조회")
  @Test
  void getQnaPostByUserIdList_Success() {
    // Given
    Long userId = 1L;
    User user = new User(); // Mock User 객체 생성
    PageRequest pageable = PageRequest.of(0, 5, Sort.by(Direction.DESC, "createdAt"));

    // Mock 설정
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(qnaPostRepository.findByUser(user, pageable)).thenReturn(Page.empty());

    // When
    Page<QnaPostDto> result = qnaPostService.getQnaPostByUserIdList(userId, pageable, null);

    // Then
    assertNotNull(result);
    verify(qnaPostRepository).findByUser(user, pageable); // 정렬 포함된 Pageable로 확인
  }

  @DisplayName("getQnaPostByUserIdList 실패 - 사용자 없음")
  @Test
  void getQnaPostByUserIdList_Failure_UserNotFound() {
    // Given
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")); // 페이지 설정

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // When
    CustomException exception = assertThrows(CustomException.class, () ->
        qnaPostService.getQnaPostByUserIdList(userId, pageable, null)
    );

    // Then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    verify(userRepository, times(1)).findById(userId);
    verifyNoInteractions(qnaPostRepository);
  }

  // =======================================================================
  // getQnaPost 테스트
  // =======================================================================
  @DisplayName("getQnaPost 성공 - 게시글 조회")
  @Test
  void getQnaPost_Success() {
    // Given
    Long postId = 1L;

    // QnaPost 객체 초기화
    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");

    QnaPost qnaPost = new QnaPost();
    qnaPost.setId(postId);
    qnaPost.setTitle("Test Title");
    qnaPost.setContent("Test Content");
    qnaPost.setUser(user);

    // Mock 설정
    when(qnaPostRepository.findById(postId)).thenReturn(Optional.of(qnaPost));

    // When
    QnaPostDto result = qnaPostService.getQnaPost(postId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(postId);
    assertThat(result.getTitle()).isEqualTo("Test Title");
    assertThat(result.getContent()).isEqualTo("Test Content");
    assertThat(result.getUser().getId()).isEqualTo(1L);
    assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");

    // Mock 검증
    verify(qnaPostRepository, times(1)).findById(postId);
  }
  @DisplayName("getQnaPost 실패 - 게시글 없음")
  @Test
  void getQnaPost_Failure_PostNotFound() {
    // Given
    Long postId = 1L;

    when(qnaPostRepository.findById(postId)).thenReturn(Optional.empty());

    // When
    CustomException exception = assertThrows(CustomException.class, () ->
        qnaPostService.getQnaPost(postId)
    );

    // Then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
    verify(qnaPostRepository, times(1)).findById(postId);
  }

  // =======================================================================
  // updateQnaPost 테스트
  // =======================================================================
  @DisplayName("updateQnaPost 성공 - 게시글 수정")
  @Test
  void updateQnaPost_Success() {
    // Given
    Long postId = 1L;
    QnaPostUpdateDto updateDto = new QnaPostUpdateDto();
    updateDto.setTitle("Updated Title");
    updateDto.setAuthor("user@example.com"); // 이메일 추가

    User user = new User();
    user.setId(1L);

    QnaPost qnaPost = new QnaPost();
    qnaPost.setId(postId);
    qnaPost.setTitle("Original Title");
    qnaPost.setUser(user);

    // Mock 설정
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(qnaPostRepository.findById(postId)).thenReturn(Optional.of(qnaPost));
    when(photoService.save(any())).thenReturn("http://s3.amazonaws.com/test/updated-thumbnail.jpg");
    when(qnaPostRepository.save(any())).thenReturn(qnaPost);

    // When
    QnaPostDto result = qnaPostService.updateQnaPost(postId, updateDto);

    // Then
    assertThat(result.getTitle()).isEqualTo("Updated Title");
    assertThat(result.getId()).isEqualTo(postId);
    assertThat(result.getUser().getId()).isEqualTo(user.getId());

    verify(qnaPostRepository, times(1)).save(argThat(post ->
        post.getTitle().equals("Updated Title") &&
            post.getUser().getId().equals(1L)
    ));
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 생성 - 성공")
  void testCreateQnaPostComment_Success() {
    // given
    Long qnaPostId = 1L;
    QnaCommentRequest qnaCommentRequest = QnaCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();
    QnaPost qnaPost = QnaPost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .qnaPost(qnaPost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
    when(qnaPostRepository.findById(eq(qnaPostId)))
        .thenReturn(Optional.of(qnaPost));
    when(qnaCommentRepository.save(any(QnaComment.class))).thenReturn(qnaComment);

    // when
    QnaCommentDto qnaPostComment = qnaPostService.createQnaPostComment(
        qnaPostId, qnaCommentRequest);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));
    verify(qnaPostRepository, times(1)).findById(eq(qnaPostId));
    verify(qnaCommentRepository, times(1))
        .save(any(QnaComment.class));

    assertEquals(1L, qnaPostComment.getId());
    assertEquals(1L, qnaPostComment.getPostId());
    assertEquals(false, qnaPostComment.getIsSecret());
    assertEquals("testComment", qnaPostComment.getContent());
    assertEquals(1L, qnaPostComment.getUser().getId());
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 생성 - 실패 (존재하지 않는 유저)")
  void testCreateQnaPostComment_Fail_UserNotFound() {
    // given
    Long qnaPostId = 1L;
    QnaCommentRequest qnaCommentRequest = QnaCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPostComment(qnaPostId, qnaCommentRequest));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));

    assertEquals(ErrorCode.USER_NOT_FOUND, customException.getErrorCode());
    assertEquals("사용자를 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 생성 - 실패 (존재하지 않는 게시글)")
  void testCreateQnaPostComment_Fail_PostNotFound() {
    // given
    Long qnaPostId = 1L;
    QnaCommentRequest qnaCommentRequest = QnaCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
    when(qnaPostRepository.findById(eq(1L))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPostComment(qnaPostId, qnaCommentRequest));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));
    verify(qnaPostRepository, times(1)).findById(eq(1L));

    assertEquals(ErrorCode.POST_NOT_FOUND, customException.getErrorCode());
    assertEquals("게시글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 조회 - 성공")
  void testGetQnaPostComment_Success() {
    // given
    Long qnaPostId = 1L;

    Pageable pageable = PageRequest.of(0, 12, Sort.by("createdAt").ascending());

    User user1 = User.builder().id(1L).nickname("testUser1").build();
    User user2 = User.builder().id(2L).nickname("testUser2").build();
    QnaPost qnaPost = QnaPost.builder()
        .id(1L)
        .title("Test Title")
        .user(user1)
        .build();

    List<QnaComment> commentList = List.of(
        QnaComment.builder()
            .id(1L)
            .qnaPost(qnaPost)
            .user(user1)
            .replies(Collections.emptyList())
            .build(),
        QnaComment.builder()
            .id(2L)
            .qnaPost(qnaPost)
            .user(user2)
            .replies(Collections.emptyList())
            .build()
    );

    Page<QnaComment> responseCommentList = new PageImpl<>(commentList);

    when(qnaPostRepository.findById(eq(qnaPostId)))
        .thenReturn(Optional.of(qnaPost));
    when(qnaCommentRepository.findAllByQnaPost(eq(qnaPost), eq(pageable)))
        .thenReturn(responseCommentList);

    // when
    Page<QnaCommentResponse> qnaPostComments = qnaPostService.getQnaPostComments(qnaPostId, 0);

    // then
    verify(qnaPostRepository, times(1)).findById(eq(qnaPostId));
    verify(qnaCommentRepository, times(1))
        .findAllByQnaPost(eq(qnaPost), eq(pageable));

    Assertions.assertNotNull(qnaPostComments);
    assertEquals(2, qnaPostComments.getSize());
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 조회 - 실패 (존재하지 않느 게시글)")
  void testGetQnaPostComment_Fail_PostNotFound() {
    // given
    Long qnaPostId = 1L;

    when(qnaPostRepository.findById(eq(qnaPostId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.getQnaPostComments(qnaPostId, 0));

    // then
    verify(qnaPostRepository, times(1)).findById(eq(qnaPostId));

    assertEquals(ErrorCode.POST_NOT_FOUND, customException.getErrorCode());
    assertEquals("게시글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 수정 - 성공")
  void testUpdateQnaPostComment_Success() {
    // given
    Long qnaCommentId = 1L;
    QnaCommentRequest qnaCommentRequest = QnaCommentRequest.builder()
        .isSecret(true)
        .content("updateComment")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();
    QnaPost qnaPost = QnaPost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .qnaPost(qnaPost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(qnaCommentRepository.findById(eq(qnaCommentId))).thenReturn(Optional.of(qnaComment));
    when(authService.getLoginUserId()).thenReturn(1L);
    when(qnaCommentRepository.save(any(QnaComment.class))).thenReturn(qnaComment);

    // when
    QnaCommentDto qnaCommentDto = 
        qnaPostService.updateQnaPostComment(qnaCommentId, qnaCommentRequest);

    // then
    verify(qnaCommentRepository, times(1)).findById(eq(qnaCommentId));
    verify(authService, times(1)).getLoginUserId();
    verify(qnaCommentRepository, times(1)).save(any(QnaComment.class));

    assertEquals(1L, qnaCommentDto.getId());
    assertEquals(1L, qnaCommentDto.getPostId());
    assertEquals(true, qnaCommentDto.getIsSecret());
    assertEquals("updateComment", qnaCommentDto.getContent());
    assertEquals(1L, qnaCommentDto.getUser().getId());
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 수정 - 실패 (존재하지 않는 댓글)")
  void testUpdateQnaPostComment_Fail_CommentNotFound() {
    // given
    Long qnaCommentId = 1L;
    QnaCommentRequest qnaCommentRequest = QnaCommentRequest.builder()
        .isSecret(true)
        .content("updateComment")
        .build();

    when(qnaCommentRepository.findById(eq(qnaCommentId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.updateQnaPostComment(qnaCommentId, qnaCommentRequest));

    // then
    verify(qnaCommentRepository, times(1)).findById(eq(qnaCommentId));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 수정 - 실패 (로그인한 유저와 불일치)")
  void testUpdateQnaPostComment_Fail_UuAuthorizedAccess() {
    // given
    Long qnaCommentId = 1L;
    QnaCommentRequest qnaCommentRequest = QnaCommentRequest.builder()
        .isSecret(true)
        .content("updateComment")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();
    QnaPost qnaPost = QnaPost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .qnaPost(qnaPost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(qnaCommentRepository.findById(eq(qnaCommentId))).thenReturn(Optional.of(qnaComment));
    when(authService.getLoginUserId()).thenReturn(2L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.updateQnaPostComment(qnaCommentId, qnaCommentRequest));

    // then
    verify(qnaCommentRepository, times(1)).findById(eq(qnaCommentId));

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, customException.getErrorCode());
    assertEquals("접근 권한이 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 삭제 - 성공")
  void testDeleteQnaPostComment_Success() {
    // given
    Long qnaCommentId = 1L;

    User user = User.builder().id(1L).nickname("testUser").build();
    QnaPost qnaPost = QnaPost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .qnaPost(qnaPost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(qnaCommentRepository.findById(eq(qnaCommentId))).thenReturn(Optional.of(qnaComment));
    when(authService.getLoginUserId()).thenReturn(1L);
    doNothing().when(qnaReplyRepository).deleteAllByComment(eq(qnaComment));
    doNothing().when(qnaCommentRepository).delete(eq(qnaComment));

    // when
    QnaCommentDto qnaPostComment = qnaPostService.deleteQnaPostComment(qnaCommentId);

    // then
    verify(qnaCommentRepository, times(1)).findById(eq(qnaCommentId));
    verify(authService, times(1)).getLoginUserId();
    verify(qnaReplyRepository, times(1)).deleteAllByComment(eq(qnaComment));
    verify(qnaCommentRepository, times(1)).delete(eq(qnaComment));
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 삭제 - 실패 (존재하지 않는 댓글)")
  void testDeleteQnaPostComment_Fail_CommentNotFound() {
    // given
    Long qnaCommentId = 1L;

    when(qnaCommentRepository.findById(eq(qnaCommentId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.deleteQnaPostComment(qnaCommentId));

    // then
    verify(qnaCommentRepository, times(1)).findById(eq(qnaCommentId));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 댓글 삭제 - 실패 (로그인한 유저와 불일치)")
  void testDeleteQnaPostComment_Fail_UnAuthorizeAccess() {
    // given
    Long qnaCommentId = 1L;

    User user = User.builder().id(1L).nickname("testUser").build();
    QnaPost qnaPost = QnaPost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .qnaPost(qnaPost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(qnaCommentRepository.findById(eq(qnaCommentId))).thenReturn(Optional.of(qnaComment));
    when(authService.getLoginUserId()).thenReturn(2L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.deleteQnaPostComment(qnaCommentId));

    // then
    verify(qnaCommentRepository, times(1)).findById(eq(qnaCommentId));
    verify(authService, times(1)).getLoginUserId();

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, customException.getErrorCode());
    assertEquals("접근 권한이 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 대댓글 생성 - 성공")
  void testCreateQnaPostReply_Success() {
    // given
    Long qnaCommentId = 1L;
    QnaReplyRequest qnaReplyRequest = QnaReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    QnaReply qnaReply = QnaReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(qnaComment)
        .build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
    when(qnaCommentRepository.findById(eq(qnaCommentId))).thenReturn(Optional.of(qnaComment));
    when(qnaReplyRepository.save(any(QnaReply.class))).thenReturn(qnaReply);

    // when
    QnaReplyDto qnaPostReply = qnaPostService.createQnaPostReply(qnaCommentId, qnaReplyRequest);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));
    verify(qnaCommentRepository, times(1)).findById(eq(qnaCommentId));
    verify(qnaReplyRepository, times(1)).save(any(QnaReply.class));

    assertEquals(1L, qnaPostReply.getId());
    assertEquals(1L, qnaPostReply.getCommentId());
    assertEquals(false, qnaPostReply.getIsSecret());
    assertEquals("testReply", qnaPostReply.getContent());
    assertEquals(1L, qnaPostReply.getUser().getId());
  }

  @Test
  @DisplayName("질의응답 게시글 대댓글 생성 - 실패 (존재하지 않는 유저)")
  void testCreateQnaPostReply_Fail_UserNotFound() {
    // given
    Long qnaCommentId = 1L;
    QnaReplyRequest qnaReplyRequest = QnaReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPostReply(qnaCommentId, qnaReplyRequest));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));

    assertEquals(ErrorCode.USER_NOT_FOUND, customException.getErrorCode());
    assertEquals("사용자를 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 대댓글 생성 - 실패 (존재하지 않는 댓글)")
  void testCreateQnaPostReply_Fail_CommentNotFound() {
    // given
    Long qnaCommentId = 1L;
    QnaReplyRequest qnaReplyRequest = QnaReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
    when(qnaCommentRepository.findById(eq(qnaCommentId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPostReply(qnaCommentId, qnaReplyRequest));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 대댓글 수정 - 성공")
  void testUpdateQnaPostReply_Success() {
    // given
    Long replyId = 1L;
    QnaReplyRequest qnaReplyRequest = QnaReplyRequest.builder()
        .isSecret(true)
        .content("updateReply")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    QnaReply qnaReply = QnaReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(qnaComment)
        .build();

    when(qnaReplyRepository.findById(eq(replyId))).thenReturn(Optional.of(qnaReply));
    when(authService.getLoginUserId()).thenReturn(1L);
    when(qnaReplyRepository.save(any(QnaReply.class))).thenReturn(qnaReply);

    // when
    QnaReplyDto qnaPostComment = qnaPostService.updateQnaPostReply(replyId, qnaReplyRequest);

    // then
    verify(qnaReplyRepository, times(1)).findById(eq(replyId));
    verify(authService, times(1)).getLoginUserId();
    verify(qnaReplyRepository, times(1)).save(any(QnaReply.class));

    assertEquals(1L, qnaPostComment.getId());
    assertEquals(1L, qnaPostComment.getCommentId());
    assertEquals(true, qnaPostComment.getIsSecret());
    assertEquals("updateReply", qnaPostComment.getContent());
    assertEquals(1L, qnaPostComment.getUser().getId());
  }

  @Test
  @DisplayName("질의응답 게시글 대댓글 수정 - 실패 (존재하지 않는 대댓글)")
  void testUpdateQnaPostReply_Fail_ReplyNotFound() {
    // given
    Long replyId = 1L;
    QnaReplyRequest qnaReplyRequest = QnaReplyRequest.builder()
        .isSecret(true)
        .content("updateReply")
        .build();

    when(qnaReplyRepository.findById(eq(replyId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.updateQnaPostReply(replyId, qnaReplyRequest));

    // then
    verify(qnaReplyRepository, times(1)).findById(eq(replyId));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 대댓글 수정 - 실패 (로그인한 유저와 불일치)")
  void testUpdateQnaPostReply_Fail_UnAuthorizeAccess() {
    // given
    Long replyId = 1L;
    QnaReplyRequest qnaReplyRequest = QnaReplyRequest.builder()
        .isSecret(true)
        .content("updateReply")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    QnaReply qnaReply = QnaReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(qnaComment)
        .build();

    when(qnaReplyRepository.findById(eq(replyId))).thenReturn(Optional.of(qnaReply));
    when(authService.getLoginUserId()).thenReturn(2L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.updateQnaPostReply(replyId, qnaReplyRequest));

    // then
    verify(qnaReplyRepository, times(1))
        .findById(eq(replyId));
    verify(authService, times(1)).getLoginUserId();

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, customException.getErrorCode());
    assertEquals("접근 권한이 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 대댓글 삭제 - 성공")
  void testDeleteQnaPostReply_Success() {
    // given
    Long replyId = 1L;

    User user = User.builder().id(1L).nickname("testUser").build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    QnaReply qnaReply = QnaReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(qnaComment)
        .build();

    when(qnaReplyRepository.findById(eq(replyId))).thenReturn(Optional.of(qnaReply));
    when(authService.getLoginUserId()).thenReturn(1L);
    doNothing().when(qnaReplyRepository).delete(eq(qnaReply));

    // when
    QnaReplyDto qnaPostComment = qnaPostService.deleteQnaPostReply(
        replyId);

    // then
    verify(qnaReplyRepository, times(1)).findById(eq(replyId));
    verify(authService, times(1)).getLoginUserId();
    verify(qnaReplyRepository, times(1)).delete(eq(qnaReply));
  }

  @Test
  @DisplayName("질의응답 게시글 대댓글 삭제 - 실패 (존재하지 않는 대댓글)")
  void testDeleteQnaPostReply_Fail_ReplyNotFound() {
    // given
    Long replyId = 1L;

    when(qnaReplyRepository.findById(eq(replyId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.deleteQnaPostReply(replyId));

    // then
    verify(qnaReplyRepository, times(1)).findById(eq(replyId));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("질의응답 게시글 대댓글 삭제 - 실패 (로그인한 사용자와 불일치)")
  void testDeleteQnaPostReply_Fail_UnAuthorizeAccess() {
    // given
    Long replyId = 1L;

    User user = User.builder().id(1L).nickname("testUser").build();

    QnaComment qnaComment = QnaComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    QnaReply qnaReply = QnaReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(qnaComment)
        .build();

    when(qnaReplyRepository.findById(eq(replyId))).thenReturn(Optional.of(qnaReply));
    when(authService.getLoginUserId()).thenReturn(2L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> qnaPostService.deleteQnaPostReply(replyId));

    // then
    verify(qnaReplyRepository, times(1))
        .findById(eq(replyId));
    verify(authService, times(1)).getLoginUserId();

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, customException.getErrorCode());
    assertEquals("접근 권한이 없습니다.", customException.getErrorMessage());
  }

}