package com.devonoff.domain.qnapost.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import com.devonoff.type.PostType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

class QnaPostServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private QnaPostRepository qnaPostRepository;

  @Mock
  private PhotoService photoService;

  @InjectMocks
  private QnaPostService qnaPostService;

  private User user;
  private User anotherUser;
  private QnaPost qnaPost;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    user = User.builder()
        .id(1L)
        .email("test@example.com")
        .build();

    anotherUser = User.builder()
        .id(2L)
        .email("another@example.com")
        .build();

    qnaPost = QnaPost.builder()
        .id(1L)
        .title("Test Title")
        .content("Test Content")
        .thumbnailUrl("test-url")
        .user(user)
        .postType(PostType.QNA_POST)
        .build();
  }

  @Test
  @DisplayName("게시글 수정 성공 테스트")
  void shouldUpdateQnaPostSuccessfully() {
    // Given
    MultipartFile mockThumbnail = mock(MultipartFile.class);
    when(mockThumbnail.getOriginalFilename()).thenReturn("updated-thumbnail.jpg");

    QnaPostUpdateDto updateDto = QnaPostUpdateDto.builder()
        .title("Updated Title")
        .content("Updated Content")
        .thumbnail(mockThumbnail)
        .build();

    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(qnaPost));
    when(photoService.save(any())).thenReturn("updated-thumbnail-url");
    when(qnaPostRepository.save(any())).thenReturn(qnaPost); // 반환값 설정

    // When
    var updatedPost = qnaPostService.updateQnaPost(1L, updateDto, user);

    // Then
    assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
    assertThat(updatedPost.getThumbnailUrl()).isEqualTo("updated-thumbnail-url");
    verify(photoService, times(1)).delete("test-url");
    verify(photoService, times(1)).save(mockThumbnail);
    verify(qnaPostRepository, times(1)).save(qnaPost);
  }

  @Test
  @DisplayName("게시글 수정 실패 테스트 - 권한 없음")
  void shouldFailToUpdateQnaPostWhenNotOwner() {
    // Given
    QnaPostUpdateDto updateDto = QnaPostUpdateDto.builder()
        .title("Updated Title")
        .content("Updated Content")
        .thumbnail(mock(MultipartFile.class))
        .build();

    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(qnaPost));

    // When
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.updateQnaPost(1L, updateDto, anotherUser));

    // Then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    verify(photoService, never()).delete(any());
    verify(photoService, never()).save(any());
  }

  @Test
  @DisplayName("게시글 생성 실패 테스트 - 제목 누락")
  void createQnaPost_FailDueToMissingTitle() {
    // Given
    QnaPostRequest request = QnaPostRequest.builder()
        .content("Content without title")
        .thumbnail(mock(MultipartFile.class))
        .build();

    // When
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPost(request, user));

    // Then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    verify(qnaPostRepository, never()).save(any());
  }

  @Test
  @DisplayName("게시글 생성 실패 테스트 - 제목과 내용 모두 누락")
  void createQnaPost_FailDueToMissingTitleAndContent() {
    // Given
    QnaPostRequest request = QnaPostRequest.builder()
        .title("")
        .content("")
        .thumbnail(mock(MultipartFile.class))
        .build();

    // When
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPost(request, user));

    // Then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    verify(qnaPostRepository, never()).save(any());
    verify(photoService, never()).save(any());
  }

  @Test
  @DisplayName("게시글 생성 실패 테스트 - 내용 누락")
  void shouldFailToCreateQnaPostWhenContentIsMissing() {
    // Given: 내용이 없는 게시글 요청
    QnaPostRequest request = QnaPostRequest.builder()
        .title("Valid Title") // 제목 있음
        .content("")          // 내용 없음
        .thumbnail(mock(MultipartFile.class))
        .build();

    // When: 게시글 생성 요청 시
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPost(request, user));

    // Then: INVALID_INPUT_VALUE 에러 발생
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    verify(qnaPostRepository, never()).save(any());
    verify(photoService, never()).save(any());
  }

  @Test
  @DisplayName("게시글 조회 실패 테스트 - 게시글 없음")
  void shouldFailToGetQnaPostWhenPostNotFound() {
    // Given: 존재하지 않는 게시글 ID 요청
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.empty());

    // When: 게시글 조회 요청 시
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.getQnaPost(1L));

    // Then: POST_NOT_FOUND 에러 발생
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
    verify(qnaPostRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("게시글 조회 성공 테스트")
  void shouldGetQnaPostSuccessfully() {
    // Given: 존재하는 게시글 ID 요청
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(qnaPost));

    // When: 게시글 조회 요청 시
    QnaPostDto result = qnaPostService.getQnaPost(1L);

    // Then: 정상적으로 게시글 반환
    assertThat(result.getId()).isEqualTo(qnaPost.getId());
    assertThat(result.getTitle()).isEqualTo(qnaPost.getTitle());
    assertThat(result.getContent()).isEqualTo(qnaPost.getContent());
    verify(qnaPostRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("게시글 생성 실패 테스트 - 제목 없음")
  void shouldFailToCreateQnaPostWhenTitleIsMissing() {
    // Given: 제목이 없는 게시글 요청
    QnaPostRequest request = QnaPostRequest.builder()
        .title("") // 제목 없음
        .content("Content")
        .thumbnail(mock(MultipartFile.class))
        .build();

    // When: 게시글 생성 요청 시
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPost(request, user));

    // Then: INVALID_INPUT_VALUE 에러 발생
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    verify(qnaPostRepository, never()).save(any());
  }

  @Test
  @DisplayName("게시글 생성 성공 테스트")
  void shouldCreateQnaPostSuccessfully() {
    // Given
    QnaPostRequest request = QnaPostRequest.builder()
        .title("Valid Title")
        .content("Valid Content")
        .thumbnail(mock(MultipartFile.class))
        .build();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(photoService.save(any())).thenReturn("uploaded-thumbnail-url");

    // When
    qnaPostService.createQnaPost(request, user);

    // Then
    verify(qnaPostRepository, times(1)).save(any(QnaPost.class));
    verify(photoService, times(1)).save(any());
  }
}