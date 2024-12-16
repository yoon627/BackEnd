package com.devonoff.domain.infosharepost.repository;

import com.devonoff.domain.infosharepost.entity.InfoShareComment;
import com.devonoff.domain.infosharepost.entity.InfoShareReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoShareReplyRepository extends JpaRepository<InfoShareReply, Long> {

  void deleteAllByComment(InfoShareComment comment);

}
