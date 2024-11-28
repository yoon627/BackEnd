
package com.devonoff.domain.qnapost.dto;


import com.devonoff.domain.qnapost.entity.QnaPost;
import com.devonoff.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaPostDto {

  private Long id;
  private String title;
  private String content;
  private String thumbnailUrl;
  private User user;


  public static QnaPostDto fromEntity(QnaPost qnaPost) {
    return QnaPostDto.builder()
        .id(qnaPost.getId())
        .title(qnaPost.getTitle())
        .content(qnaPost.getContent())
        .thumbnailUrl(qnaPost.getThumbnailUrl())
        .user(qnaPost.getUser())
        .build();
  }
}
