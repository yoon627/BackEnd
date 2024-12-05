package com.devonoff.domain.qnapost.service;

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
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
   * 토큰 X
   *
   * @param page   조회할 페이지 번호 (1부터 시작)
   * @param search 검색 키워드 (optional)
   * @return Page<QnaPostDto>
   */
  public Page<QnaPostDto> getQnaPostList(Integer page, String search) {

    // 페이지 번호 유효성 검사
    if (page == null || page < 1) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 요청입니다.");
    }

    // 페이지네이션 및 정렬 설정
    Pageable pageable = PageRequest.of(page - 1, 5, Sort.by(Sort.Direction.DESC, "createdAt"));

    // 검색 조건에 따라 전체 게시물 또는 검색 결과 반환
    if (search == null || search.isBlank() || search.equals("")) {
      return qnaPostRepository.findAll(pageable)
          .map(QnaPostDto::fromEntity);
    }

    return qnaPostRepository.findByTitleContaining(search.trim(), pageable)
        .map(QnaPostDto::fromEntity);
  }
  /**
   * 특정 사용자의 질의 응답 게시글 목록 조회 (최신순)
   * 토큰O
   * @param userId
   * @param page
   * @param search
   * @return Page<QnaPostDto>
   */
  public Page<QnaPostDto> getQnaPostByUserIdList(Long userId, Integer page, String search) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

    Sort sort = Sort.by(Direction.DESC, "createdAt");
    Pageable pageable = PageRequest.of(page - 1, 5, sort);

    // 검색어가 없을 경우와 있을 경우 구분
    Page<QnaPost> posts = (search != null && !search.isBlank())
        ? qnaPostRepository.findByUserAndTitleContaining(user, search, pageable)
        : qnaPostRepository.findByUser(user, pageable);

    // posts가 null인 경우 처리
    if (posts == null) {
      posts = new PageImpl<>(Collections.emptyList());
    }

    return posts.map(QnaPostDto::fromEntity);
  }

  /**
   * 특정 질의 응답 게시글 상세 조회
   * 토큰 X
   * @param qnaPostId
   * @return QnaPostDto
   */
  public QnaPostDto getQnaPost(Long qnaPostId) {
    QnaPost qnaPost = qnaPostRepository.findById(qnaPostId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    return QnaPostDto.fromEntity(qnaPost);
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



