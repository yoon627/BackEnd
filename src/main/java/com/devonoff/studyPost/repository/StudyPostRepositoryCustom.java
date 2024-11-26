package com.devonoff.studyPost.repository;

import com.devonoff.studyPost.dto.StudyPostDto;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import java.util.List;

public interface StudyPostRepositoryCustom {

  List<StudyPostDto> findStudyPostsByFilters(String title, StudySubject subject,
      StudyDifficulty difficulty, int dayType,
      StudyStatus status);
}