package com.devonoff.domain.reply.Repository;

import com.devonoff.domain.reply.entity.Reply;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

  List<Reply> findByCommentId(Long commentId);
}

