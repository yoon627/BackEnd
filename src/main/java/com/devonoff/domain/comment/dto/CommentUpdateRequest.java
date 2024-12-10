package com.devonoff.domain.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 업데이트 요청값
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {

  @JsonProperty("is_secret")
  private Boolean isSecret;

  @JsonProperty("content")
  private String content;
}