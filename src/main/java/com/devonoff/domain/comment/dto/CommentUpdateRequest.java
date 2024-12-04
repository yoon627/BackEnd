package com.devonoff.domain.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

// 업데이트 요청값
@Getter
@Setter
public class CommentUpdateRequest {

  @JsonProperty("is_secret")
  private Boolean isSecret;

  @JsonProperty("content")
  private String content;
}