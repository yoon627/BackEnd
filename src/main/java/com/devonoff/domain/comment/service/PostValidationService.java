package com.devonoff.domain.comment.service;


import com.devonoff.domain.infosharepost.repository.InfoSharePostRepository;
import com.devonoff.domain.qnapost.repository.QnaPostRepository;
import com.devonoff.domain.studyPost.repository.StudyPostRepository;
import com.devonoff.exception.CustomException;
import com.devonoff.type.ErrorCode;
import com.devonoff.type.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 댓글 검증서비스 분리

@Service
@RequiredArgsConstructor
public class PostValidationService {

  private final StudyPostRepository studyPostRepository;
  private final QnaPostRepository qnaPostRepository;
  private final InfoSharePostRepository infoSharePostRepository;

  public void validatePostExists(PostType postType, Long postId) {
    boolean exists;
    switch (postType) {
      case QNA -> exists = qnaPostRepository.existsById(postId);
      case STUDY -> exists = studyPostRepository.existsById(postId);
      case INFO -> exists = infoSharePostRepository.existsById(postId);
      default -> throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 게시글 타입입니다.");
    }

    if (!exists) {
      throw new CustomException(ErrorCode.POST_NOT_FOUND, "게시글이 존재하지 않습니다.");
    }
  }

}