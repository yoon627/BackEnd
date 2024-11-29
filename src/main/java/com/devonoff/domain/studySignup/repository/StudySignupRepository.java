package com.devonoff.domain.studySignup.repository;

import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.domain.user.entity.User;
import com.devonoff.type.StudySignupStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudySignupRepository extends JpaRepository<StudySignup, Long> {

  boolean existsByStudyPostAndUser(StudyPost studyPost, User user);

  List<StudySignup> findByStudyPost(StudyPost studyPost);

  List<StudySignup> findByStudyPostAndStatus(StudyPost studyPost, StudySignupStatus status);
}
