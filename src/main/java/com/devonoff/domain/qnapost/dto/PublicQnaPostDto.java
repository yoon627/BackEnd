package com.devonoff.domain.qnapost.dto;

import com.devonoff.domain.qnapost.entity.QnaPost;
import com.devonoff.type.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicQnaPostDto {

  private Long id; // 게시글 ID
  private String title; // 게시글 제목
  private String thumbnailUrl; // 썸네일 URL
  private String nickName; // 작성자 닉네임 (공개 가능한 정보만 사용)
  private PostType postType; // 게시글 타입

  /**
   * QnaPost에서 PublicQnaPostDto로 변환
   */
  public static PublicQnaPostDto fromEntity(QnaPost qnaPost) {
    return PublicQnaPostDto.builder()
        .id(qnaPost.getId())
        .title(qnaPost.getTitle())
        .thumbnailUrl(qnaPost.getThumbnailUrl())
        .nickName(qnaPost.getUser().getNickname()) // User의 닉네임만 포함
        .postType(qnaPost.getPostType())
        .build();
  }
}