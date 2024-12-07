package com.devonoff.domain.comment.repository;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.type.PostType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findByPostIdAndPostType(Long postId, PostType postType);

  // 특정 게시글의 댓글과 대댓글을 함께 조회
  @EntityGraph(attributePaths = {"replies"})
  Page<Comment> findByPostIdAndPostType(Long postId, PostType postType, Pageable pageable);

}