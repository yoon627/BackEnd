package com.devonoff.domain.comment.repository;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.type.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findByPostIdAndPostType(Long postId, PostType postType);
 // 조회 메서드
 Page<Comment> findByPostIdAndPostType(Long postId, PostType postType, Pageable pageable);
}