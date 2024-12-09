package com.devonoff.domain.study.repository;

import com.devonoff.domain.study.entity.Study;
import com.devonoff.type.StudyStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {

  Page<Study> findByStudentsUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  List<Study> findAllByStatusAndStartDateBefore(StudyStatus status, LocalDateTime now);

  List<Study> findAllByStatusAndEndDateBefore(StudyStatus status, LocalDateTime now);
}
