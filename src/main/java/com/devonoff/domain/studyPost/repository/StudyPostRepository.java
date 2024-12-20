package com.devonoff.domain.studyPost.repository;

import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.domain.user.entity.User;
import com.devonoff.type.StudyPostStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostRepository extends JpaRepository<StudyPost, Long>,
    StudyPostRepositoryCustom {

  void deleteByStatusAndUpdatedAtBefore(StudyPostStatus status, LocalDateTime dateTime);

  List<StudyPost> findAllByRecruitmentPeriodBeforeAndStatus(LocalDate recruitmentPeriod,
      StudyPostStatus status);

  Page<StudyPost> findByUserId(Long userId, Pageable pageable);
  Page<StudyPost> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
  List<StudyPost> findAllByUser(User user);

  List<StudyPost> findAllByStatusAndUpdatedAtBefore(StudyPostStatus status, LocalDateTime updatedAt);
}