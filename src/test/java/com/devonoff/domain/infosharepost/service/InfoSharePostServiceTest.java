package com.devonoff.domain.infosharepost.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.entity.InfoShareComment;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.repository.InfoShareCommentRepository;
import com.devonoff.domain.infosharepost.repository.InfoSharePostRepository;
import com.devonoff.domain.infosharepost.repository.InfoShareReplyRepository;
import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentDto;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentRequest;
import com.devonoff.domain.infosharepost.dto.InfoShareCommentResponse;
import com.devonoff.domain.infosharepost.dto.InfoShareReplyDto;
import com.devonoff.domain.infosharepost.dto.InfoShareReplyRequest;
import com.devonoff.domain.infosharepost.entity.InfoShareComment;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.entity.InfoShareReply;
import com.devonoff.domain.user.dto.UserDto;
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
import org.springframework.web.multipart.MultipartFile;

class InfoSharePostServiceTest {

  @InjectMocks
  private InfoSharePostService infoSharePostService;

  @Mock
  private InfoSharePostRepository infoSharePostRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private InfoShareCommentRepository infoShareCommentRepository;

  @Mock
  private InfoShareReplyRepository infoShareReplyRepository;

  @Mock
  private PhotoService photoService;

  @Mock
  private AuthService authService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("정보공유 게시글 생성 - 성공")
  void testCreateInfoSharePost_Success() {
    // given
    MultipartFile file = mock(MultipartFile.class);
    InfoSharePostDto dto = InfoSharePostDto.builder()
        .title("Test Title")
        .description("Test Description")
        .build();
    User user = User.builder().id(1L).nickname("testuser").build();
    InfoSharePost entity = InfoSharePost.builder().title("Test Title").user(user).build();

    when(authService.getLoginUserId()).thenReturn(1L); // Mock 로그인 사용자 ID
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(photoService.save(file)).thenReturn("test-url");
    when(infoSharePostRepository.save(any())).thenReturn(entity);

    // when
    InfoSharePostDto result = infoSharePostService.createInfoSharePost(dto);

    // then
    assertNotNull(result);
    assertEquals("Test Title", result.getTitle());
    verify(infoSharePostRepository).save(any());
  }

  @Test
  @DisplayName("정보공유 게시글 생성 - 실패(사용자가 존재하지 않음)")
  void testCreateInfoSharePost_UserNotFound_ThrowsException() {
    // given
    MultipartFile file = mock(MultipartFile.class);
    InfoSharePostDto dto = InfoSharePostDto.builder()
        .title("Test Title")
        .description("Test Description")
        .build();

    when(authService.getLoginUserId()).thenReturn(1L); // Mock 로그인 사용자 ID
    when(userRepository.findById(1L)).thenReturn(Optional.empty()); // 사용자 없음

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        infoSharePostService.createInfoSharePost(dto));

    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("정보공유 게시글 수정 - 실패(권한 없음)")
  void testUpdateInfoSharePost_UnauthorizedAccess_ThrowsException() {
    // given
    Long postId = 1L;
    InfoSharePostDto dto = InfoSharePostDto.builder()
        .user(UserDto.builder().id(2L).build()) // 다른 사용자의 ID
        .build();
    MultipartFile file = mock(MultipartFile.class);

    when(authService.getLoginUserId()).thenReturn(1L); // Mock 로그인 사용자 ID

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        infoSharePostService.updateInfoSharePost(postId, dto));

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
  }

  @Test
  @DisplayName("정보공유 게시글 삭제 - 성공")
  void testDeleteInfoSharePost_Success() {
    // given
    InfoSharePost post = InfoSharePost.builder().user(User.builder().id(1L).build()).build();

    List<InfoShareComment> commentList = List.of(
        InfoShareComment.builder().id(1L).infoSharePost(post).build(),
        InfoShareComment.builder().id(2L).infoSharePost(post).build()
    );

    when(infoSharePostRepository.findById(1L)).thenReturn(Optional.of(post));
    when(authService.getLoginUserId()).thenReturn(1L); // Mock 로그인 사용자 ID
    doNothing().when(photoService).delete(anyString());
    when(infoShareCommentRepository.findAllByInfoSharePost(eq(post)))
        .thenReturn(commentList);
    doNothing().when(infoShareReplyRepository).deleteAllByComment(any(InfoShareComment.class));
    doNothing().when(infoShareCommentRepository).deleteAllByInfoSharePost(eq(post));

    // when
    infoSharePostService.deleteInfoSharePost(1L);

    // then
    verify(infoSharePostRepository).deleteById(1L);
  }

  @Test
  @DisplayName("정보공유 게시글 삭제 - 실패(권한 없음)")
  void testDeleteInfoSharePost_UnauthorizedAccess_ThrowsException() {
    // given
    InfoSharePost post = InfoSharePost.builder().user(User.builder().id(2L).build()).build();

    when(authService.getLoginUserId()).thenReturn(1L); // Mock 로그인 사용자 ID
    when(infoSharePostRepository.findById(1L)).thenReturn(Optional.of(post)); // 게시물 작성자는 다른 사용자

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        infoSharePostService.deleteInfoSharePost(1L));

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
  }


  @Test
  @DisplayName("정보공유 게시글 댓글 생성 - 성공")
  void testCreateInfoSharePostComment_Success() {
    // given
    Long infoSharePostId = 1L;
    InfoShareCommentRequest infoShareCommentRequest = InfoShareCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();
    InfoSharePost infoSharePost = InfoSharePost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .infoSharePost(infoSharePost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
    when(infoSharePostRepository.findById(eq(infoSharePostId)))
        .thenReturn(Optional.of(infoSharePost));
    when(infoShareCommentRepository.save(any(InfoShareComment.class))).thenReturn(infoShareComment);

    // when
    InfoShareCommentDto infoSharePostComment = infoSharePostService.createInfoSharePostComment(
        infoSharePostId, infoShareCommentRequest);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));
    verify(infoSharePostRepository, times(1)).findById(eq(infoSharePostId));
    verify(infoShareCommentRepository, times(1))
        .save(any(InfoShareComment.class));

    assertEquals(1L, infoSharePostComment.getId());
    assertEquals(1L, infoSharePostComment.getPostId());
    assertEquals(false, infoSharePostComment.getIsSecret());
    assertEquals("testComment", infoSharePostComment.getContent());
    assertEquals(1L, infoSharePostComment.getUser().getId());
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 생성 - 실패 (존재하지 않는 유저)")
  void testCreateInfoSharePostComment_Fail_UserNotFound() {
    // given
    Long infoSharePostId = 1L;
    InfoShareCommentRequest infoShareCommentRequest = InfoShareCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.createInfoSharePostComment(infoSharePostId, infoShareCommentRequest));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));

    assertEquals(ErrorCode.USER_NOT_FOUND, customException.getErrorCode());
    assertEquals("사용자를 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 생성 - 실패 (존재하지 않는 게시글)")
  void testCreateInfoSharePostComment_Fail_PostNotFound() {
    // given
    Long infoSharePostId = 1L;
    InfoShareCommentRequest infoShareCommentRequest = InfoShareCommentRequest.builder()
        .isSecret(false)
        .content("testComment")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
    when(infoSharePostRepository.findById(eq(1L))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.createInfoSharePostComment(infoSharePostId, infoShareCommentRequest));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));
    verify(infoSharePostRepository, times(1)).findById(eq(1L));

    assertEquals(ErrorCode.POST_NOT_FOUND, customException.getErrorCode());
    assertEquals("게시글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 조회 - 성공")
  void testGetInfoSharePostComment_Success() {
    // given
    Long infoSharePostId = 1L;

    Pageable pageable = PageRequest.of(0, 12, Sort.by("createdAt").ascending());

    User user1 = User.builder().id(1L).nickname("testUser1").build();
    User user2 = User.builder().id(2L).nickname("testUser2").build();
    InfoSharePost infoSharePost = InfoSharePost.builder()
        .id(1L)
        .title("Test Title")
        .user(user1)
        .build();

    List<InfoShareComment> commentList = List.of(
        InfoShareComment.builder()
            .id(1L)
            .infoSharePost(infoSharePost)
            .user(user1)
            .replies(Collections.emptyList())
            .build(),
        InfoShareComment.builder()
            .id(2L)
            .infoSharePost(infoSharePost)
            .user(user2)
            .replies(Collections.emptyList())
            .build()
    );

    Page<InfoShareComment> responseCommentList = new PageImpl<>(commentList);

    when(infoSharePostRepository.findById(eq(infoSharePostId)))
        .thenReturn(Optional.of(infoSharePost));
    when(infoShareCommentRepository.findAllByInfoSharePost(eq(infoSharePost), eq(pageable)))
        .thenReturn(responseCommentList);

    // when
    Page<InfoShareCommentResponse> infoSharePostComments =
        infoSharePostService.getInfoSharePostComments(infoSharePostId, 0);

    // then
    verify(infoSharePostRepository, times(1)).findById(eq(infoSharePostId));
    verify(infoShareCommentRepository, times(1))
        .findAllByInfoSharePost(eq(infoSharePost), eq(pageable));

    Assertions.assertNotNull(infoSharePostComments);
    assertEquals(2, infoSharePostComments.getSize());
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 조회 - 실패 (존재하지 않느 게시글)")
  void testGetInfoSharePostComment_Fail_PostNotFound() {
    // given
    Long infoSharePostId = 1L;

    when(infoSharePostRepository.findById(eq(infoSharePostId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.getInfoSharePostComments(infoSharePostId, 0));

    // then
    verify(infoSharePostRepository, times(1)).findById(eq(infoSharePostId));

    assertEquals(ErrorCode.POST_NOT_FOUND, customException.getErrorCode());
    assertEquals("게시글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 수정 - 성공")
  void testUpdateInfoSharePostComment_Success() {
    // given
    Long infoShareCommentId = 1L;
    InfoShareCommentRequest infoShareCommentRequest = InfoShareCommentRequest.builder()
        .isSecret(true)
        .content("updateComment")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();
    InfoSharePost infoSharePost = InfoSharePost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .infoSharePost(infoSharePost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(infoShareCommentRepository.findById(eq(infoShareCommentId))).thenReturn(Optional.of(infoShareComment));
    when(authService.getLoginUserId()).thenReturn(1L);
    when(infoShareCommentRepository.save(any(InfoShareComment.class))).thenReturn(infoShareComment);

    // when
    InfoShareCommentDto infoShareCommentDto =
        infoSharePostService.updateInfoSharePostComment(infoShareCommentId, infoShareCommentRequest);

    // then
    verify(infoShareCommentRepository, times(1)).findById(eq(infoShareCommentId));
    verify(authService, times(1)).getLoginUserId();
    verify(infoShareCommentRepository, times(1)).save(any(InfoShareComment.class));

    assertEquals(1L, infoShareCommentDto.getId());
    assertEquals(1L, infoShareCommentDto.getPostId());
    assertEquals(true, infoShareCommentDto.getIsSecret());
    assertEquals("updateComment", infoShareCommentDto.getContent());
    assertEquals(1L, infoShareCommentDto.getUser().getId());
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 수정 - 실패 (존재하지 않는 댓글)")
  void testUpdateInfoSharePostComment_Fail_CommentNotFound() {
    // given
    Long infoShareCommentId = 1L;
    InfoShareCommentRequest infoShareCommentRequest = InfoShareCommentRequest.builder()
        .isSecret(true)
        .content("updateComment")
        .build();

    when(infoShareCommentRepository.findById(eq(infoShareCommentId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.updateInfoSharePostComment(infoShareCommentId, infoShareCommentRequest));

    // then
    verify(infoShareCommentRepository, times(1)).findById(eq(infoShareCommentId));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 수정 - 실패 (로그인한 유저와 불일치)")
  void testUpdateInfoSharePostComment_Fail_UuAuthorizedAccess() {
    // given
    Long infoShareCommentId = 1L;
    InfoShareCommentRequest infoShareCommentRequest = InfoShareCommentRequest.builder()
        .isSecret(true)
        .content("updateComment")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();
    InfoSharePost infoSharePost = InfoSharePost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .infoSharePost(infoSharePost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(infoShareCommentRepository.findById(eq(infoShareCommentId))).thenReturn(Optional.of(infoShareComment));
    when(authService.getLoginUserId()).thenReturn(2L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.updateInfoSharePostComment(infoShareCommentId, infoShareCommentRequest));

    // then
    verify(infoShareCommentRepository, times(1)).findById(eq(infoShareCommentId));

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, customException.getErrorCode());
    assertEquals("접근 권한이 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 삭제 - 성공")
  void testDeleteInfoSharePostComment_Success() {
    // given
    Long infoShareCommentId = 1L;

    User user = User.builder().id(1L).nickname("testUser").build();
    InfoSharePost infoSharePost = InfoSharePost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .infoSharePost(infoSharePost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(infoShareCommentRepository.findById(eq(infoShareCommentId))).thenReturn(Optional.of(infoShareComment));
    when(authService.getLoginUserId()).thenReturn(1L);
    doNothing().when(infoShareReplyRepository).deleteAllByComment(eq(infoShareComment));
    doNothing().when(infoShareCommentRepository).delete(eq(infoShareComment));

    // when
    InfoShareCommentDto infoSharePostComment = infoSharePostService.deleteInfoSharePostComment(infoShareCommentId);

    // then
    verify(infoShareCommentRepository, times(1)).findById(eq(infoShareCommentId));
    verify(authService, times(1)).getLoginUserId();
    verify(infoShareReplyRepository, times(1)).deleteAllByComment(eq(infoShareComment));
    verify(infoShareCommentRepository, times(1)).delete(eq(infoShareComment));
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 삭제 - 실패 (존재하지 않는 댓글)")
  void testDeleteInfoSharePostComment_Fail_CommentNotFound() {
    // given
    Long infoShareCommentId = 1L;

    when(infoShareCommentRepository.findById(eq(infoShareCommentId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.deleteInfoSharePostComment(infoShareCommentId));

    // then
    verify(infoShareCommentRepository, times(1)).findById(eq(infoShareCommentId));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 댓글 삭제 - 실패 (로그인한 유저와 불일치)")
  void testDeleteInfoSharePostComment_Fail_UnAuthorizeAccess() {
    // given
    Long infoShareCommentId = 1L;

    User user = User.builder().id(1L).nickname("testUser").build();
    InfoSharePost infoSharePost = InfoSharePost.builder()
        .id(1L)
        .title("Test Title")
        .user(user)
        .build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .infoSharePost(infoSharePost)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    when(infoShareCommentRepository.findById(eq(infoShareCommentId))).thenReturn(Optional.of(infoShareComment));
    when(authService.getLoginUserId()).thenReturn(2L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.deleteInfoSharePostComment(infoShareCommentId));

    // then
    verify(infoShareCommentRepository, times(1)).findById(eq(infoShareCommentId));
    verify(authService, times(1)).getLoginUserId();

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, customException.getErrorCode());
    assertEquals("접근 권한이 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 대댓글 생성 - 성공")
  void testCreateInfoSharePostReply_Success() {
    // given
    Long infoShareCommentId = 1L;
    InfoShareReplyRequest infoShareReplyRequest = InfoShareReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    InfoShareReply infoShareReply = InfoShareReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(infoShareComment)
        .build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
    when(infoShareCommentRepository.findById(eq(infoShareCommentId))).thenReturn(Optional.of(infoShareComment));
    when(infoShareReplyRepository.save(any(InfoShareReply.class))).thenReturn(infoShareReply);

    // when
    InfoShareReplyDto infoSharePostReply =
        infoSharePostService.createInfoSharePostReply(infoShareCommentId, infoShareReplyRequest);

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));
    verify(infoShareCommentRepository, times(1)).findById(eq(infoShareCommentId));
    verify(infoShareReplyRepository, times(1)).save(any(InfoShareReply.class));

    assertEquals(1L, infoSharePostReply.getId());
    assertEquals(1L, infoSharePostReply.getCommentId());
    assertEquals(false, infoSharePostReply.getIsSecret());
    assertEquals("testReply", infoSharePostReply.getContent());
    assertEquals(1L, infoSharePostReply.getUser().getId());
  }

  @Test
  @DisplayName("정보공유 게시글 대댓글 생성 - 실패 (존재하지 않는 유저)")
  void testCreateInfoSharePostReply_Fail_UserNotFound() {
    // given
    Long infoShareCommentId = 1L;
    InfoShareReplyRequest infoShareReplyRequest = InfoShareReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.createInfoSharePostReply(infoShareCommentId, infoShareReplyRequest));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));

    assertEquals(ErrorCode.USER_NOT_FOUND, customException.getErrorCode());
    assertEquals("사용자를 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 대댓글 생성 - 실패 (존재하지 않는 댓글)")
  void testCreateInfoSharePostReply_Fail_CommentNotFound() {
    // given
    Long infoShareCommentId = 1L;
    InfoShareReplyRequest infoShareReplyRequest = InfoShareReplyRequest.builder()
        .isSecret(false)
        .content("testCommentReply")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    when(authService.getLoginUserId()).thenReturn(1L);
    when(userRepository.findById(eq(1L))).thenReturn(Optional.of(user));
    when(infoShareCommentRepository.findById(eq(infoShareCommentId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.createInfoSharePostReply(infoShareCommentId, infoShareReplyRequest));

    // then
    verify(authService, times(1)).getLoginUserId();
    verify(userRepository, times(1)).findById(eq(1L));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 대댓글 수정 - 성공")
  void testUpdateInfoSharePostReply_Success() {
    // given
    Long replyId = 1L;
    InfoShareReplyRequest infoShareReplyRequest = InfoShareReplyRequest.builder()
        .isSecret(true)
        .content("updateReply")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    InfoShareReply infoShareReply = InfoShareReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(infoShareComment)
        .build();

    when(infoShareReplyRepository.findById(eq(replyId))).thenReturn(Optional.of(infoShareReply));
    when(authService.getLoginUserId()).thenReturn(1L);
    when(infoShareReplyRepository.save(any(InfoShareReply.class))).thenReturn(infoShareReply);

    // when
    InfoShareReplyDto infoSharePostComment = infoSharePostService.updateInfoSharePostReply(replyId, infoShareReplyRequest);

    // then
    verify(infoShareReplyRepository, times(1)).findById(eq(replyId));
    verify(authService, times(1)).getLoginUserId();
    verify(infoShareReplyRepository, times(1)).save(any(InfoShareReply.class));

    assertEquals(1L, infoSharePostComment.getId());
    assertEquals(1L, infoSharePostComment.getCommentId());
    assertEquals(true, infoSharePostComment.getIsSecret());
    assertEquals("updateReply", infoSharePostComment.getContent());
    assertEquals(1L, infoSharePostComment.getUser().getId());
  }

  @Test
  @DisplayName("정보공유 게시글 대댓글 수정 - 실패 (존재하지 않는 대댓글)")
  void testUpdateInfoSharePostReply_Fail_ReplyNotFound() {
    // given
    Long replyId = 1L;
    InfoShareReplyRequest infoShareReplyRequest = InfoShareReplyRequest.builder()
        .isSecret(true)
        .content("updateReply")
        .build();

    when(infoShareReplyRepository.findById(eq(replyId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.updateInfoSharePostReply(replyId, infoShareReplyRequest));

    // then
    verify(infoShareReplyRepository, times(1)).findById(eq(replyId));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 대댓글 수정 - 실패 (로그인한 유저와 불일치)")
  void testUpdateInfoSharePostReply_Fail_UnAuthorizeAccess() {
    // given
    Long replyId = 1L;
    InfoShareReplyRequest infoShareReplyRequest = InfoShareReplyRequest.builder()
        .isSecret(true)
        .content("updateReply")
        .build();

    User user = User.builder().id(1L).nickname("testUser").build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    InfoShareReply infoShareReply = InfoShareReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(infoShareComment)
        .build();

    when(infoShareReplyRepository.findById(eq(replyId))).thenReturn(Optional.of(infoShareReply));
    when(authService.getLoginUserId()).thenReturn(2L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.updateInfoSharePostReply(replyId, infoShareReplyRequest));

    // then
    verify(infoShareReplyRepository, times(1))
        .findById(eq(replyId));
    verify(authService, times(1)).getLoginUserId();

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, customException.getErrorCode());
    assertEquals("접근 권한이 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 대댓글 삭제 - 성공")
  void testDeleteInfoSharePostReply_Success() {
    // given
    Long replyId = 1L;

    User user = User.builder().id(1L).nickname("testUser").build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    InfoShareReply infoShareReply = InfoShareReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(infoShareComment)
        .build();

    when(infoShareReplyRepository.findById(eq(replyId))).thenReturn(Optional.of(infoShareReply));
    when(authService.getLoginUserId()).thenReturn(1L);
    doNothing().when(infoShareReplyRepository).delete(eq(infoShareReply));

    // when
    InfoShareReplyDto infoSharePostComment = infoSharePostService.deleteInfoSharePostReply(
        replyId);

    // then
    verify(infoShareReplyRepository, times(1)).findById(eq(replyId));
    verify(authService, times(1)).getLoginUserId();
    verify(infoShareReplyRepository, times(1)).delete(eq(infoShareReply));
  }

  @Test
  @DisplayName("정보공유 게시글 대댓글 삭제 - 실패 (존재하지 않는 대댓글)")
  void testDeleteInfoSharePostReply_Fail_ReplyNotFound() {
    // given
    Long replyId = 1L;

    when(infoShareReplyRepository.findById(eq(replyId))).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.deleteInfoSharePostReply(replyId));

    // then
    verify(infoShareReplyRepository, times(1)).findById(eq(replyId));

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, customException.getErrorCode());
    assertEquals("댓글을 찾을 수 없습니다.", customException.getErrorMessage());
  }

  @Test
  @DisplayName("정보공유 게시글 대댓글 삭제 - 실패 (로그인한 사용자와 불일치)")
  void testDeleteInfoSharePostReply_Fail_UnAuthorizeAccess() {
    // given
    Long replyId = 1L;

    User user = User.builder().id(1L).nickname("testUser").build();

    InfoShareComment infoShareComment = InfoShareComment.builder()
        .id(1L)
        .isSecret(false)
        .content("testComment")
        .user(user)
        .build();

    InfoShareReply infoShareReply = InfoShareReply.builder()
        .id(1L)
        .isSecret(false)
        .content("testReply")
        .user(user)
        .comment(infoShareComment)
        .build();

    when(infoShareReplyRepository.findById(eq(replyId))).thenReturn(Optional.of(infoShareReply));
    when(authService.getLoginUserId()).thenReturn(2L);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> infoSharePostService.deleteInfoSharePostReply(replyId));

    // then
    verify(infoShareReplyRepository, times(1))
        .findById(eq(replyId));
    verify(authService, times(1)).getLoginUserId();

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, customException.getErrorCode());
    assertEquals("접근 권한이 없습니다.", customException.getErrorMessage());
  }

}
