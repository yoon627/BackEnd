package com.devonoff.domain.studyTimeline.repository;

import com.devonoff.domain.studyTimeline.entity.StudyTimeline;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTimelineRepository extends JpaRepository<StudyTimeline, Long> {

  List<StudyTimeline> findAllByStudyIdAndEndedAtIsNotNull(Long studyId);
}
