package com.devonoff.domain.qnapost.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaPostRequest {
  private String author;
  private MultipartFile thumbnail;
  private String title;
  private String content;
  //private PostType postType;
}
