package com.devonoff.domain.reply.Repository;

import com.devonoff.domain.comment.entity.Comment;
import com.devonoff.domain.reply.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

  void deleteAllByComment(Comment comment);
}

