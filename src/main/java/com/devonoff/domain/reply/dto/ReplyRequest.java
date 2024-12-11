package com.devonoff.domain.reply.dto;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.reply.entity.Reply;
import com.devonoff.domain.user.entity.User;
import com.devonoff.type.PostType;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ReplyRequest {

  @JsonProperty("is_secret")
  private Boolean isSecret;
  @JsonProperty("post_id")
  private Long postId;  // 게시글 ID

  @JsonProperty("post_type")
  private String postType;
  private String content;  // 댓글 내용

  // Reply 엔티티로 변환
  public Reply toEntity(User user, Comment comment) {
    return Reply.builder()
        .postType(PostType.valueOf(this.postType))
        .isSecret(this.isSecret)
        .postId(this.postId)
        .content(this.content)
        .user(user)
        .comment(comment)
        .build();
  }

}