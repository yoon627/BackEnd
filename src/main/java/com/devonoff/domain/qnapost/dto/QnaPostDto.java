
package com.devonoff.domain.qnapost.dto;


import com.devonoff.domain.qnapost.entity.QnaPost;
import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.type.PostType;
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
  private UserDto userDto;
  private PostType postType;


  public static QnaPostDto fromEntity(QnaPost qnaPost) {
    return QnaPostDto.builder()
        .id(qnaPost.getId())
        .title(qnaPost.getTitle())
        .content(qnaPost.getContent())
        .thumbnailUrl(qnaPost.getThumbnailUrl())
        .userDto(UserDto.fromEntity(qnaPost.getUser()))
        .postType(qnaPost.getPostType())
        .build();
  }
}
