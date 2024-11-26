package com.devonoff.studyPost.repository;

import com.devonoff.studyPost.dto.StudyPostDto;
import com.devonoff.studyPost.entity.QStudyPost;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyPostRepositoryCustomImpl implements StudyPostRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<StudyPostDto> findStudyPostsByFilters(String title, StudySubject subject,
      StudyDifficulty difficulty, int dayType, StudyStatus status) {
    QStudyPost studyPost = QStudyPost.studyPost;

    return queryFactory.select(Projections.constructor(StudyPostDto.class,
            studyPost.id,
            studyPost.title,
            studyPost.studyName,
            studyPost.subject,
            studyPost.difficulty,
            studyPost.dayType,
            studyPost.startDate,
            studyPost.endDate,
            studyPost.startTime,
            studyPost.endTime,
            studyPost.meetingType,
            studyPost.recruitmentPeriod,
            studyPost.description,
            studyPost.latitude,
            studyPost.longitude,
            studyPost.status,
            studyPost.thumbnailImgUrl,
            studyPost.user.Id
        ))
        .from(studyPost)
        .where(
            title != null ? studyPost.title.contains(title) : null,
            subject != null ? studyPost.subject.eq(subject) : null,
            difficulty != null ? studyPost.difficulty.eq(difficulty) : null,
            dayType > 0 ? studyPost.dayType.eq(dayType) : null,
            status != null ? studyPost.status.eq(status) : null
        )
        .fetch();
  }
}