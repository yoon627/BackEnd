package com.devonoff.domain.studySignup.repository;

import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.studySignup.entity.StudySignup;
import com.devonoff.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudySignupRepository extends JpaRepository<StudySignup, Long> {

  boolean existsByStudyPostAndUser(StudyPost studyPost, User user);
}
