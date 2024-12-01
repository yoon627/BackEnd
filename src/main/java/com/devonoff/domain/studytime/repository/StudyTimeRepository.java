package com.devonoff.domain.studytime.repository;

import com.devonoff.domain.studytime.entity.StudyTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTimeRepository extends JpaRepository<StudyTime, Long> {

  List<StudyTime> findAllByStudyIdAndEndedAtIsNotNull(Long studyId);
}
