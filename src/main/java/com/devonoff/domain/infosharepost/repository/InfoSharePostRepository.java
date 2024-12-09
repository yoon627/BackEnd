package com.devonoff.domain.infosharepost.repository;

import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoSharePostRepository extends JpaRepository<InfoSharePost, Long> {

  Page<InfoSharePost> findAllByTitleContaining(String search, Pageable pageable);

  Page<InfoSharePost> findAllByUserIdAndTitleContaining(Long userId, String search,
      Pageable pageable);
}
