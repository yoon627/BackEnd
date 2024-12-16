package com.devonoff.domain.qnapost.repository;

import com.devonoff.domain.qnapost.entity.QnaComment;
import com.devonoff.domain.qnapost.entity.QnaReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QnaReplyRepository extends JpaRepository<QnaReply, Long> {

  void deleteAllByComment(QnaComment comment);

}
