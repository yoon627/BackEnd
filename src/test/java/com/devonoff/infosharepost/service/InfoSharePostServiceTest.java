package com.devonoff.infosharepost.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.repository.InfoSharePostRepository;
import com.devonoff.domain.infosharepost.service.InfoSharePostService;
import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.domain.user.service.AuthService;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

class InfoSharePostServiceTest {

  @InjectMocks
  private InfoSharePostService infoSharePostService;

  @Mock
  private InfoSharePostRepository infoSharePostRepository;

  @Mock
  private UserRepository userRepository;

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
    InfoSharePostDto result = infoSharePostService.createInfoSharePost(dto, file);

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
        infoSharePostService.createInfoSharePost(dto, file));

    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("정보공유 게시글 수정 - 실패(권한 없음)")
  void testUpdateInfoSharePost_UnauthorizedAccess_ThrowsException() {
    // given
    Long postId = 1L;
    InfoSharePostDto dto = InfoSharePostDto.builder()
        .userDto(UserDto.builder().id(2L).build()) // 다른 사용자의 ID
        .build();
    MultipartFile file = mock(MultipartFile.class);

    when(authService.getLoginUserId()).thenReturn(1L); // Mock 로그인 사용자 ID

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        infoSharePostService.updateInfoSharePost(postId, dto, file));

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
  }

  @Test
  @DisplayName("정보공유 게시글 삭제 - 성공")
  void testDeleteInfoSharePost_Success() {
    // given
    InfoSharePost post = InfoSharePost.builder().user(User.builder().id(1L).build()).build();

    when(authService.getLoginUserId()).thenReturn(1L); // Mock 로그인 사용자 ID
    when(infoSharePostRepository.findById(1L)).thenReturn(Optional.of(post));

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
}
