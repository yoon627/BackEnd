package com.devonoff.domain.comment.entity;

import com.devonoff.common.entity.BaseTimeEntity;
import com.devonoff.domain.user.entity.User;
import com.devonoff.type.PostType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Entity
public class Comment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PostType postType; // 게시글 유형

  @Column(nullable = false)
  private Long postId; // 게시글 ID

  @Column(nullable = false)
  private Boolean isSecret; // 비밀 댓글 여부

  @Column(nullable = false)
  private String content; // 댓글 내용



  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 댓글 작성자와 연결


  @Builder
  public Comment(PostType postType, Long postId, Boolean isSecret, String content, User user) {
    this.postType = postType;
    this.postId = postId;
    this.isSecret = isSecret;
    this.content = content;
    this.user = user;
  }
}