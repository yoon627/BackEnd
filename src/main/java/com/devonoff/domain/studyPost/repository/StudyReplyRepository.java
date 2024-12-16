package com.devonoff.domain.studyPost.repository;

import com.devonoff.domain.studyPost.entity.StudyComment;
import com.devonoff.domain.studyPost.entity.StudyReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyReplyRepository extends JpaRepository<StudyReply, Long> {

  void deleteAllByComment(StudyComment comment);

}
