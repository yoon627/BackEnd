package com.devonoff.studyPost.repository;

import com.devonoff.studyPost.entity.StudyPost;
import com.devonoff.type.StudyStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {

  void deleteByStatusAndUpdatedAtBefore(StudyStatus status, LocalDateTime dateTime);
}