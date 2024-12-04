package com.devonoff.domain.qnapost.service;

import com.devonoff.domain.photo.service.PhotoService;
import com.devonoff.domain.qnapost.dto.PublicQnaPostDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QnaPostService {

  private static final int QNA_PAGE_SIZE = 5;

  private final QnaPostRepository qnaPostRepository;
  private final UserRepository userRepository;
  private final PhotoService photoService;

  /**
   * 질의 응답 게시글 생성
   *
   * @param email
   * @param qnaPostRequest
   */
  @Transactional
  public ResponseEntity<Void> createQnaPost(QnaPostRequest qnaPostRequest, String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 썸네일 저장
    String uploadedThumbnailUrl = null;
    if (qnaPostRequest.getThumbnail() != null && !qnaPostRequest.getThumbnail().isEmpty()) {
      uploadedThumbnailUrl = photoService.save(qnaPostRequest.getThumbnail());
    }

    // QnaPost 저장
    QnaPost qnaPost = QnaPost.builder()
        .title(qnaPostRequest.getTitle())
        .content(qnaPostRequest.getContent())
        .thumbnailUrl(uploadedThumbnailUrl)
        .postType(PostType.QNA)
        .user(user)
        .build();

    qnaPostRepository.save(qnaPost);

    // 상태 코드만 반환
    return ResponseEntity.ok().build(); // HTTP 200
  }

  /**
   * 질의 응답 게시글 전체 목록 조회 (최신순)
   *
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  public Page<PublicQnaPostDto> getQnaPostList(Integer page, String search) {

    Sort sort = Sort.by(Direction.DESC, "createdAt");

    Pageable pageable = PageRequest.of(page - 1, QNA_PAGE_SIZE, sort);

    // search가 비어있는 경우 전체 게시물 조회
    if (search == null || search.isBlank()) {
      return qnaPostRepository.findAll(pageable)
          .map(PublicQnaPostDto::fromEntity);
    }
    return qnaPostRepository.findByTitleContaining(search, pageable)
        .map(PublicQnaPostDto::fromEntity);
  }

  /**
   * 특정 사용자의 질의 응답 게시글 목록 조회 (최신순)
   *
   * @param userId
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  public Page<PublicQnaPostDto> getQnaPostByUserIdList(Long userId, Integer page, String search) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    Sort sort = Sort.by(Direction.DESC, "createdAt");

    Pageable pageable = PageRequest.of(page - 1, QNA_PAGE_SIZE, sort);

    return qnaPostRepository.findByUserAndTitleContaining(user, search, pageable)
        .map(PublicQnaPostDto::fromEntity);
  }

  /**
   * 특정 질의 응답 게시글 상세 조회
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  public com.devonoff.domain.qnapost.dto.QnaPostDto getQnaPost(Long qnaPostId) {
    return QnaPostDto.fromEntity(
        qnaPostRepository.findById(qnaPostId).orElseThrow(() -> new CustomException(
            ErrorCode.POST_NOT_FOUND)));
  }

  /**
   * 특정 질의 응답 게시글 수정
   *
   * @param qnaPostId
   * @param qnaPostUpdateDto
   * @return QnaPostDto
   */
  @Transactional
  public QnaPostDto updateQnaPost(Long qnaPostId,
      QnaPostUpdateDto qnaPostUpdateDto) {

    // 작성자 이메일을 DTO에서 가져오기
    String email = qnaPostUpdateDto.getAuthor();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 게시글 가져오기 및 작성자 확인
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    if (!qnaPost.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "작성자만 게시글을 수정할 수 있습니다.");
    }

    // 썸네일 업데이트 로직
    String updatedThumbnailUrl = qnaPost.getThumbnailUrl();
    if (qnaPostUpdateDto.getThumbnail() != null && !qnaPostUpdateDto.getThumbnail().isEmpty()) {
      if (qnaPost.getThumbnailUrl() != null) {
        photoService.delete(qnaPost.getThumbnailUrl());
      }
      updatedThumbnailUrl = photoService.save(qnaPostUpdateDto.getThumbnail());
    }

    // 게시글 업데이트
    qnaPost.setTitle(qnaPostUpdateDto.getTitle());
    qnaPost.setContent(qnaPostUpdateDto.getContent());
    qnaPost.setThumbnailUrl(updatedThumbnailUrl);

    QnaPost updatedQnaPost = qnaPostRepository.save(qnaPost);

    return QnaPostDto.fromEntity(updatedQnaPost);
  }

  /**
   * 특정 질의 응답 게시글 삭제
   *
   * @param qnaPostId
   * @return QnaPostDto
   */
  @Transactional
  public void deleteQnaPost(Long qnaPostId) {
    // SecurityContext에서 인증된 사용자 ID 가져오기
    Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

    // 사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    // 게시글 조회 및 작성자 확인
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    if (!qnaPost.getUser().getId().equals(user.getId())) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "작성자만 게시글을 삭제할 수 있습니다.");
    }

    // 썸네일 파일 삭제
    if (qnaPost.getThumbnailUrl() != null) {
      photoService.delete(qnaPost.getThumbnailUrl());
    }

    // 게시글 삭제
    qnaPostRepository.delete(qnaPost);
  }
}



