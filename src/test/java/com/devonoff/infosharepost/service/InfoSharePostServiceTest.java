package com.devonoff.infosharepost.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Mock
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("정보공유 게시글 생성 - 성공")
  void createInfoSharePost_Success() {
    // given
    MultipartFile file = mock(MultipartFile.class);
    InfoSharePostDto dto = InfoSharePostDto.builder()
        .title("Test Title")
        .description("Test Description")
        .build();
    User user = User.builder().id(1L).nickName("testuser")
        .build(); // Ensure User is properly initialized
    InfoSharePost entity = InfoSharePost.builder().title("Test Title").user(user)
        .build(); // Associate the user with the post

    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn("1");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user)); // Return the mocked User
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
  @DisplayName("정보공유 게시글 조회 - 성공")
  void getInfoSharePosts_Success() {
    // given
    Page<InfoSharePost> page = new PageImpl<>(Collections.singletonList(
        InfoSharePost.builder().title("Test Title").user(User.builder().id(1L).build()).build()
        // Add a User to the post
    ));
    when(infoSharePostRepository.findAllByTitleContaining(eq("test"), any(Pageable.class)))
        .thenReturn(page);

    // when
    Page<InfoSharePostDto> result = infoSharePostService.getInfoSharePosts(0, "test");

    // then
    assertEquals(1, result.getTotalElements());
    assertEquals("Test Title", result.getContent().get(0).getTitle());
  }

  @Test
  @DisplayName("정보공유 게시글 수정 - 성공")
  void updateInfoSharePost_Success() {
    // given
    Long postId = 1L;
    MultipartFile file = mock(MultipartFile.class);
    InfoSharePostDto dto = InfoSharePostDto.builder()
        .title("Updated Title")
        .description("Updated Description")
        .thumbnailImgUrl("updated-url")
        .userDto(
            UserDto.builder().id(1L).build()) // Ensure the userDto contains the correct user ID
        .build();

    User user = User.builder().id(1L).nickName("testuser").build();
    InfoSharePost existingPost = InfoSharePost.builder()
        .id(postId)
        .user(user)
        .title("Old Title")
        .description("Old Description")
        .thumbnailImgUrl("old-url")
        .build();

    // Mock the behavior
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn(
        "1"); // Simulate that the authenticated user is the same as the post's user
    when(infoSharePostRepository.findById(postId)).thenReturn(Optional.of(existingPost));
    when(photoService.save(file)).thenReturn("updated-url");
    when(infoSharePostRepository.save(any())).thenReturn(
        existingPost); // Simulate saving the updated post

    // when
    InfoSharePostDto result = infoSharePostService.updateInfoSharePost(postId, dto, file);

    // then
    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    assertEquals("Updated Description", result.getDescription());
    assertEquals("updated-url", result.getThumbnailImgUrl());
    verify(infoSharePostRepository).save(any());
  }

  @Test
  @DisplayName("정보공유 게시글 수정 - 실패(권한 없음)")
  void updateInfoSharePost_UnauthorizedAccess_ThrowsException() {
    // given
    InfoSharePostDto dto = InfoSharePostDto.builder()
        .userDto(UserDto.builder().id(2L).build())
        .build();
    MultipartFile file = mock(MultipartFile.class);

    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn("1");

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        infoSharePostService.updateInfoSharePost(1L, dto, file));

    assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
  }

  @Test
  @DisplayName("정보공유 게시글 수정 - 성공")
  void deleteInfoSharePost_Success() {
    // given
    InfoSharePost post = InfoSharePost.builder().user(User.builder().id(1L).build()).build();

    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn("1");
    when(infoSharePostRepository.findById(1L)).thenReturn(Optional.of(post));

    // when
    infoSharePostService.deleteInfoSharePost(1L);

    // then
    verify(infoSharePostRepository).deleteById(1L);
  }

  @Test
  @DisplayName("정보공유 게시글 수정 - 실패(게시물이 존재하지 않음)")
  void deleteInfoSharePost_PostNotFound_ThrowsException() {
    // given
    when(infoSharePostRepository.findById(1L)).thenReturn(Optional.empty());

    // when & then
    CustomException exception = assertThrows(CustomException.class, () ->
        infoSharePostService.deleteInfoSharePost(1L));

    assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
  }
}
