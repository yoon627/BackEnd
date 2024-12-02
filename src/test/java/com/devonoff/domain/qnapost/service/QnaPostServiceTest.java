package com.devonoff.domain.qnapost.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.qnapost.dto.QnaPostRequest;
import com.devonoff.domain.qnapost.dto.QnaPostUpdateDto;
import com.devonoff.domain.qnapost.entity.QnaPost;
import com.devonoff.domain.qnapost.repository.QnaPostRepository;
import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.user.repository.UserRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

class QnaPostServiceTest {


  @Mock
  private QnaPostRepository qnaPostRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PhotoService photoService;

  @InjectMocks
  private QnaPostService qnaPostService;

  private User user;
  private QnaPost qnaPost;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    user = User.builder().id(1L).email("test@example.com").build();
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
  @DisplayName("게시글 생성 성공 테스트")
  void createQnaPost_Success() {
    // Given: 게시글 요청과 사용자 정보가 주어진 경우
    MultipartFile mockThumbnail = Mockito.mock(MultipartFile.class);
    QnaPostRequest request = QnaPostRequest.builder()
        .title("New Title")
        .content("New Content")
        .thumbnail(mockThumbnail)
        .build();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(photoService.save(any())).thenReturn("uploaded-url");

    // When: 게시글 생성 요청을 수행할 때
    var response = qnaPostService.createQnaPost(request, user);

    // Then: 생성된 메시지와 저장 로직이 호출되어야 함
    assertThat(response).containsEntry("message", "게시글 작성이 완료되었습니다.");
    verify(qnaPostRepository, times(1)).save(any(QnaPost.class));
  }

  @Test
  @DisplayName("게시글 생성 실패 테스트 - 사용자 없음")
  void createQnaPost_UserNotFound() {
    // Given: 존재하지 않는 사용자의 게시글 생성 요청이 주어진 경우
    MultipartFile mockThumbnail = mock(MultipartFile.class);
    when(mockThumbnail.getOriginalFilename()).thenReturn("thumbnail.jpg");

    QnaPostRequest request = QnaPostRequest.builder()
        .title("New Title")
        .content("New Content")
        .thumbnail(mockThumbnail)
        .build();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

    // When: 게시글 생성 요청을 수행할 때
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPost(request, user));

    // Then: 사용자 없음 오류가 발생해야 함
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    verify(photoService, never()).save(any());
    verify(qnaPostRepository, never()).save(any(QnaPost.class));
  }

  @Test
  @DisplayName("게시글 생성 실패 테스트 - 제목 누락")
  void createQnaPost_FailDueToMissingTitle() {
    // Given: 제목이 없는 게시글 요청
    QnaPostRequest request = QnaPostRequest.builder()
        .content("Content without title")
        .thumbnail(mock(MultipartFile.class))
        .build();

    // When: 생성 요청 처리
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.createQnaPost(request, user));

    // Then: 예외 발생
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    verify(qnaPostRepository, never()).save(any());
  }

  @Test
  @DisplayName("게시글 생성 실패 테스트 - 내용 누락")
  void createQnaPost_FailDueToMissingContent() {
    // Given
    QnaPostRequest request = QnaPostRequest.builder()
        .title("Valid Title")
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
  @DisplayName("게시글 수정 성공 테스트")
  void updateQnaPost_Success() {
    // Given: 존재하는 게시글과 수정 요청이 주어진 경우
    MultipartFile mockThumbnail = mock(MultipartFile.class);
    when(mockThumbnail.getOriginalFilename()).thenReturn("updated-thumbnail.jpg");

    QnaPostUpdateDto updateDto = QnaPostUpdateDto.builder()
        .title("Updated Title")
        .content("Updated Content")
        .thumbnail(mockThumbnail)
        .build();

    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(qnaPost));
    when(photoService.save(any())).thenReturn("updated-thumbnail-url");

    // When: 게시글 수정 요청을 수행할 때
    var updatedPost = qnaPostService.updateQnaPost(1L, updateDto, user);

    // Then: 게시글 내용이 수정되어야 함
    assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
    assertThat(updatedPost.getThumbnailUrl()).isEqualTo("updated-thumbnail-url");
    verify(photoService, times(1)).delete("test-url");
    verify(photoService, times(1)).save(mockThumbnail);
  }

  @Test
  @DisplayName("게시글 수정 실패 테스트 - 작성자가 아님")
  void updateQnaPost_FailDueToNotOwner() {
    // Given: 게시글의 작성자가 아닌 사용자가 수정 요청을 보낸 경우
    User anotherUser = User.builder().id(2L).build();
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(qnaPost));

    QnaPostUpdateDto updateDto = QnaPostUpdateDto.builder()
        .title("Updated Title")
        .content("Updated Content")
        .thumbnail(mock(MultipartFile.class))
        .build();

    // When: 게시글 수정 요청을 수행할 때
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.updateQnaPost(1L, updateDto, anotherUser));

    // Then: 작성자 아님 오류가 발생해야 함
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    verify(photoService, never()).delete(any());
    verify(photoService, never()).save(any());
  }
  @Test
  @DisplayName("게시글 삭제 실패 테스트 - 작성자가 아님")
  void deleteQnaPost_NotOwner2() {
    // Given
    User anotherUser = User.builder().id(2L).build();
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(qnaPost));

    // When
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.deleteQnaPost(1L, anotherUser));

    // Then
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    verify(photoService, never()).delete(any());
    verify(qnaPostRepository, never()).delete(any());
  }

  @Test
  @DisplayName("게시글 삭제 성공 테스트")
  void deleteQnaPost_Success() {
    // Given: 존재하는 게시글과 삭제 요청 사용자가 주어진 경우
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(qnaPost));

    // When: 게시글 삭제 요청을 수행할 때
    var response = qnaPostService.deleteQnaPost(1L, user);

    // Then: 게시글이 삭제되고 메시지가 반환되어야 함
    assertThat(response).containsEntry("message", "정상적으로 삭제 되었습니다.");
    verify(photoService, times(1)).delete("test-url");
    verify(qnaPostRepository, times(1)).delete(qnaPost);
  }

  @Test
  @DisplayName("게시글 수정 실패 테스트 - 게시글 없음")
  void updateQnaPost_FailDueToPostNotFound() {
    // Given: 존재하지 않는 게시글 ID로 수정 요청이 주어진 경우
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.empty());

    QnaPostUpdateDto updateDto = QnaPostUpdateDto.builder()
        .title("Updated Title")
        .content("Updated Content")
        .thumbnail(mock(MultipartFile.class))
        .build();

    // When: 게시글 수정 요청을 수행할 때
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.updateQnaPost(1L, updateDto, user));

    // Then: 게시글 없음 오류가 발생해야 함
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
    verify(photoService, never()).delete(any());
    verify(photoService, never()).save(any());
  }
  @Test
  @DisplayName("게시글 삭제 실패 테스트 - 게시글 없음")
  void deleteQnaPost_FailDueToPostNotFound() {
    // Given: 존재하지 않는 게시글 ID로 삭제 요청이 주어진 경우
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.empty());

    // When: 게시글 삭제 요청을 수행할 때
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.deleteQnaPost(1L, user));

    // Then: 게시글 없음 오류가 발생해야 함
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
    verify(photoService, never()).delete(any());
    verify(qnaPostRepository, never()).delete(any());
  }
  @Test
  @DisplayName("게시글 삭제 실패 테스트 - 작성자가 아님")
  void deleteQnaPost_NotOwner() {
    // Given: 삭제 요청 사용자가 게시글 작성자가 아닌 경우
    User anotherUser = User.builder().id(2L).build();
    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(qnaPost));

    // When: 게시글 삭제 요청을 수행할 때
    CustomException exception = assertThrows(CustomException.class,
        () -> qnaPostService.deleteQnaPost(1L, anotherUser));

    // Then: 작성자 아님 오류가 발생해야 함
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    verify(photoService, never()).delete(anyString());
    verify(qnaPostRepository, never()).delete(any());
  }

  @Test
  @DisplayName("게시글 목록 조회 테스트")
  void getQnaPostList_Success() {
    // Given: 게시글 검색 요청이 주어진 경우
    Page<QnaPost> page = new PageImpl<>(Collections.singletonList(qnaPost));

    // Mock 설정: Pageable과 검색어에 대해 반환값 지정
    when(qnaPostRepository.findByTitleContaining(anyString(), any(Pageable.class))).thenReturn(
        page);

    // When: 게시글 목록 조회를 요청할 때
    var result = qnaPostService.getQnaPostList(1, "Test");

    // Then: 게시글 목록이 반환되어야 함
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Title");
  }
}