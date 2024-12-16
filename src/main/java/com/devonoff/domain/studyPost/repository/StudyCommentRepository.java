package com.devonoff.domain.studyPost.repository;

import com.devonoff.domain.studyPost.entity.StudyComment;
import com.devonoff.domain.studyPost.entity.StudyPost;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyCommentRepository extends JpaRepository<StudyComment, Long> {

  List<StudyComment> findAllByStudyPost(StudyPost studyPost);

  Page<StudyComment> findAllByStudyPost(StudyPost studyPost, Pageable pageable);

  void deleteAllByStudyPost(StudyPost studyPost);
}
