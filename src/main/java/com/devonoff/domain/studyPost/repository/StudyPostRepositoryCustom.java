package com.devonoff.domain.studyPost.repository;

import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyPostRepositoryCustom {

  Page<StudyPostDto> findStudyPostsByFilters(
      StudyMeetingType meetingType, String title, StudySubject subject,
      StudyDifficulty difficulty, int dayType, StudyPostStatus status,
      Double latitude, Double longitude, Pageable pageable);
}