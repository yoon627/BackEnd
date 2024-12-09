package com.devonoff.domain.comment.entity;

import com.devonoff.common.entity.BaseTimeEntity;
import com.devonoff.domain.reply.entity.Reply;
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
import jakarta.persistence.OneToMany;
import java.util.List;
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


  @Column(nullable = false, length = 50)
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

  @OneToMany(mappedBy = "comment", fetch = FetchType.LAZY)
  private List<Reply> replies; // 댓글에 달린 대댓글들

}