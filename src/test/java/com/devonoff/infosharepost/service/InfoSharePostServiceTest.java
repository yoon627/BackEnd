package com.devonoff.infosharepost.service;

import static com.devonoff.type.ErrorCode.POST_NOT_FOUND;
import static com.devonoff.type.ErrorCode.UNAUTHORIZED_ACCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.infosharepost.dto.InfoSharePostDto;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import com.devonoff.domain.infosharepost.repository.InfoSharePostRepository;
import com.devonoff.domain.infosharepost.service.InfoSharePostService;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

class InfoSharePostServiceTest {

  @InjectMocks
  private InfoSharePostService infoSharePostService;

  @Mock
  private InfoSharePostRepository infoSharePostRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Mock
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn("1"); // Mocked user ID
  }

  @Test
  void createInfoSharePost_success() {
    // given
    InfoSharePostDto postDto = new InfoSharePostDto();
    postDto.setTitle("Test Title");
    postDto.setDescription("Test Description");
    postDto.setThumbnailImgUrl("test-url");

    InfoSharePost savedPost = new InfoSharePost();
    savedPost.setId(1L);
    savedPost.setTitle("Test Title");

    when(infoSharePostRepository.save(any())).thenReturn(savedPost);

    // when
    InfoSharePostDto result = infoSharePostService.createInfoSharePost(postDto);

    // then
    assertThat(result.getTitle()).isEqualTo("Test Title");
    verify(infoSharePostRepository, times(1)).save(any());
  }

  @Test
  void getInfoSharePostByPostId_notFound() {
    // given
    when(infoSharePostRepository.findById(1L)).thenReturn(Optional.empty());

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> infoSharePostService.getInfoSharePostByPostId(1L));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(POST_NOT_FOUND);
  }

  @Test
  void updateInfoSharePost_unauthorizedAccess() {
    // given
    InfoSharePostDto postDto = new InfoSharePostDto();
    postDto.setUserId(2L); // Different user ID

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> infoSharePostService.updateInfoSharePost(1L, postDto));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(UNAUTHORIZED_ACCESS);
  }

  @Test
  void deleteInfoSharePost_success() {
    // given
    InfoSharePost post = new InfoSharePost();
    post.setId(1L);
    post.setUserId(1L); // Same user ID

    when(infoSharePostRepository.findById(1L)).thenReturn(Optional.of(post));

    // when
    infoSharePostService.deleteInfoSharePost(1L);

    // then
    verify(infoSharePostRepository, times(1)).deleteById(1L);
  }

  @Test
  void getInfoSharePostsByUserId_success() {
    // given
    PageRequest pageable = PageRequest.of(0, 10);
    InfoSharePost post = new InfoSharePost();
    post.setId(1L);
    post.setUserId(1L);
    post.setTitle("Test Post");

    User user = new User();
    user.setId(1L);
    user.setNickName("Test User");

    when(infoSharePostRepository.findAllByUserIdAndTitleContaining(1L, "", pageable))
        .thenReturn(new PageImpl<>(Collections.singletonList(post)));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // when
    Page<InfoSharePostDto> result = infoSharePostService.getInfoSharePostsByUserId(1L, 0, "");

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Post");
  }
}