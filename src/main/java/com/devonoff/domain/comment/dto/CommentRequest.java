package com.devonoff.domain.comment.dto;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.user.entity.User;
import com.devonoff.type.PostType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 // 요청값

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {

  private String author;

  @JsonProperty("is_secret")
  private Boolean isSecret;

  @JsonProperty("post_id")
  private Long postId;

  @JsonProperty("post_type")
  private PostType postType;

  @JsonProperty("content")
  private String content;

  /**
   * DTO -> Entity 변환
   *
   * @param user 작성자 정보
   * @return Comment 엔티티
   */
  public Comment toEntity(User user) {
    return Comment.builder()
        .postType(this.postType)
        .postId(this.postId)
        .isSecret(this.isSecret)
        .content(this.content)
        .user(user)
        .build();
  }
}