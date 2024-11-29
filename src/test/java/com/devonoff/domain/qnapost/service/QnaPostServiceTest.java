//package com.devonoff.domain.qnapost.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.mockito.internal.verification.VerificationModeFactory.times;
//
//import com.devonoff.domain.photo.service.PhotoService;
//import com.devonoff.domain.qnapost.dto.QnaPostDto;
//import com.devonoff.domain.qnapost.dto.QnaPostRequest;
//import com.devonoff.domain.qnapost.dto.QnaPostUpdateDto;
//import com.devonoff.domain.qnapost.entity.QnaPost;
//import com.devonoff.domain.qnapost.repository.QnaPostRepository;
//import com.devonoff.domain.user.entity.User;
//import com.devonoff.domain.user.repository.UserRepository;
//import com.devonoff.exception.CustomException;
//import com.devonoff.type.ErrorCode;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.mock.web.MockMultipartFile;
//
//public class QnaPostServiceTest {
//
//  @Mock
//  private QnaPostRepository qnaPostRepository;
//
//  @Mock
//  private UserRepository userRepository;
//
//  @Mock
//  private PhotoService photoService;
//
//  @InjectMocks
//  private QnaPostService qnaPostService;
//
//  @BeforeEach
//  void setUp() {
//    MockitoAnnotations.openMocks(this);
//  }
//
//
//  @Test
//  void createQnaPost_Success() {
//    // given
//    User mockUser = new User();
//    mockUser.setEmail("test@example.com");
//
//    MockMultipartFile mockFile = new MockMultipartFile(
//        "file", "thumbnail.png", "image/png", "dummy content".getBytes()
//    );
//
//    QnaPostRequest request = new QnaPostRequest();
//    //request.setAuthor("test@example.com");
//    request.setTitle("Sample Title");
//    request.setContent("Sample Content");
//    request.setThumbnail(mockFile);
//
//    //when(userRepository.findByEmail(request.getAuthor())).thenReturn(Optional.of(mockUser));
//    when(photoService.save(mockFile)).thenReturn("saved-thumbnail-url");
//
//    // when
//    Map<String, String> response = qnaPostService.createQnaPost(request);
//
//    // then
//    String expected = "게시글 작성이 완료되었습니다.";
//    assertThat(response.get("message")).isEqualTo(expected);
//    verify(qnaPostRepository, times(1)).save(any(QnaPost.class));  // 저장 호출 검증
//    }
//  /**
//   * 게시글 작성 실패 테스트 - 사용자 없음
//   */
//  @Test
//  void createQnaPost_UserNotFound() {
//    // given
//    QnaPostRequest request = new QnaPostRequest();
//    //request.setAuthor("nonexistent@example.com");
//
//    //when(userRepository.findByEmail(request.getAuthor())).thenReturn(
//        //Optional.empty()); // Mock: 사용자 찾지 못함
//
//    // when & then
//    CustomException exception = assertThrows(CustomException.class,
//        () -> qnaPostService.createQnaPost(request)); // 예외 발생 확인
//    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND); // 예외 코드 검증
//  }
//
//  /**
//   * 게시글 전체 목록 조회 성공 테스트
//   */
//  @Test
//  void getQnaPostList_Success() {
//    // given
//    QnaPost mockPost = new QnaPost();
//    mockPost.setTitle("Test Post");
//    mockPost.setContent("Test Content");
//
//    List<QnaPost> mockPosts = List.of(mockPost);
//    Page<QnaPost> mockPage = new PageImpl<>(mockPosts); // Mock: Page 객체 생성
//
//    Pageable pageable = PageRequest.of(0, 5); // Pageable 설정
//    when(qnaPostRepository.findByTitleContaining(anyString(), any(Pageable.class)))
//        .thenReturn(mockPage); // Mock: 반환값 설정
//
//    // when
//    Page<QnaPostDto> result = qnaPostService.getQnaPostList(1, "Test");
//
//    // then
//    assertThat(result.getTotalElements()).isEqualTo(1); // 총 게시글 개수 검증
//    assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Post"); // 게시글 제목 검증
//  }
//
//  /**
//   * 게시글 수정 성공 테스트
//   */
//  @Test
//  void updateQnaPost_Success() {
//    // given
//    QnaPost mockPost = new QnaPost();
//    mockPost.setThumbnailUrl("old-thumbnail");
//
//    MockMultipartFile mockFile = new MockMultipartFile(
//        "file", "new-thumbnail.png", "image/png", "dummy content".getBytes()
//    );
//
//    QnaPostUpdateDto updateDto = new QnaPostUpdateDto();
//    updateDto.setThumbnail(mockFile); // MockMultipartFile 전달
//    updateDto.setTitle("Updated Title");
//    updateDto.setContent("Updated Content");
//
//    when(qnaPostRepository.findById(anyLong())).thenReturn(Optional.of(mockPost)); // Mock: 게시글 찾기
//    when(photoService.save(mockFile)).thenReturn("new-thumbnail-url"); // Mock: 썸네일 저장
//
//    // when
//    QnaPostDto updatedPost = qnaPostService.updateQnaPost(1L, updateDto);
//
//    // then
//    assertThat(updatedPost.getTitle()).isEqualTo("Updated Title"); // 수정된 제목 검증
//    verify(photoService, times(1)).delete("old-thumbnail");        // 이전 썸네일 삭제 호출 확인
//  }
//
//  /**
//   * 게시글 삭제 성공 테스트
//   */
//  @Test
//  void deleteQnaPost_Success() {
//    // given
//    QnaPost mockPost = new QnaPost();
//    mockPost.setThumbnailUrl("thumbnail-url");
//
//    when(qnaPostRepository.findById(1L)).thenReturn(Optional.of(mockPost)); // Mock: 게시글 찾기
//
//    // when
//    Map<String, String> response = qnaPostService.deleteQnaPost(1L);
//
//    // then
//    assertThat(response.get("message")).isEqualTo("정상적으로 삭제 되었습니다."); // 응답 메시지 검증
//    verify(photoService, times(1)).delete("thumbnail-url");                 // 썸네일 삭제 호출 확인
//    verify(qnaPostRepository, times(1)).delete(mockPost);                   // 게시글 삭제 호출 확인
//  }
//
//  /**
//   * 게시글 삭제 실패 테스트 - 게시글 없음
//   */
//  @Test
//  void deleteQnaPost_NotFound() {
//    // given
//    when(qnaPostRepository.findById(anyLong())).thenReturn(Optional.empty()); // Mock: 게시글 찾지 못함
//
//    // when & then
//    CustomException exception = assertThrows(CustomException.class,
//        () -> qnaPostService.deleteQnaPost(1L)); // 예외 발생 확인
//    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND); // 예외 코드 검증
//  }
//}