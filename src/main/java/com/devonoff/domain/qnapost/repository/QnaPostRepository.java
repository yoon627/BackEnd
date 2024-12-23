package com.devonoff.domain.qnapost.repository;

import com.devonoff.domain.user.entity.User;
import com.devonoff.domain.qnapost.entity.QnaPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QnaPostRepository extends JpaRepository<QnaPost, Long> {

  Page<QnaPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<QnaPost> findByTitleContainingOrderByCreatedAtDesc(String search, Pageable pageable);

  Page<QnaPost> findByUserAndTitleContainingOrderByCreatedAtDesc(User user, String title, Pageable pageable);

  Page<QnaPost> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
