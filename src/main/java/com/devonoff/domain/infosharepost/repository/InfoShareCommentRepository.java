package com.devonoff.domain.infosharepost.repository;

import com.devonoff.domain.infosharepost.entity.InfoShareComment;
import com.devonoff.domain.infosharepost.entity.InfoSharePost;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoShareCommentRepository extends JpaRepository<InfoShareComment, Long> {

  List<InfoShareComment> findAllByInfoSharePost(InfoSharePost infoSharePost);

  Page<InfoShareComment> findAllByInfoSharePost(InfoSharePost infoSharePost, Pageable pageable);

  void deleteAllByInfoSharePost(InfoSharePost infoSharePost);
}
