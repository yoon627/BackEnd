package com.devonoff.domain.studyPost.repository;

import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.type.StudyPostStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostRepository extends JpaRepository<StudyPost, Long>,
    StudyPostRepositoryCustom {

  void deleteByStatusAndUpdatedAtBefore(StudyPostStatus status, LocalDateTime dateTime);

  List<StudyPost> findAllByRecruitmentPeriodBeforeAndStatus(LocalDate recruitmentPeriod,
      StudyPostStatus status);
}