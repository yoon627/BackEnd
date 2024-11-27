package com.devonoff.studyPost.repository;

import com.devonoff.studyPost.dto.StudyPostDto;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface StudyPostRepositoryCustom {

  List<StudyPostDto> findStudyPostsByFilters(
      StudyMeetingType meetingType, String title, StudySubject subject,
      StudyDifficulty difficulty, int dayType, StudyStatus status,
      Double latitude, Double longitude, Pageable pageable);
}