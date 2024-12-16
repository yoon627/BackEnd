package com.devonoff.domain.qnapost.repository;

import com.devonoff.domain.qnapost.entity.QnaComment;
import com.devonoff.domain.qnapost.entity.QnaPost;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QnaCommentRepository extends JpaRepository<QnaComment, Long> {

  List<QnaComment> findAllByQnaPost(QnaPost qnaPost);

  Page<QnaComment> findAllByQnaPost(QnaPost qnaPost, Pageable pageable);

  void deleteAllByQnaPost(QnaPost qnaPost);
}
