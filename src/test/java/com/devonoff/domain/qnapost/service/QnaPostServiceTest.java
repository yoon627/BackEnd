package com.devonoff.domain.qnapost.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.qnapost.dto.QnaPostDto;
import com.devonoff.domain.qnapost.dto.QnaPostRequest;
import com.devonoff.domain.qnapost.dto.QnaPostUpdateDto;
import com.devonoff.domain.qnapost.entity.QnaPost;
import com.devonoff.domain.qnapost.repository.QnaPostRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

class QnaPostServiceTest {

  @Mock
  private QnaPostRepository qnaPostRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PhotoService photoService;

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
  }}