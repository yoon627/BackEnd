package com.devonoff.studyPost.repository;

import com.devonoff.studyPost.dto.StudyPostDto;
import com.devonoff.studyPost.entity.QStudyPost;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import com.querydsl.core.types.dsl.BooleanExpression;
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

    return queryFactory
        .selectFrom(studyPost)
        .where(
            containsTitle(title),
            equalsSubject(subject),
            equalsDifficulty(difficulty),
            equalsStatus(status)
        )
        .fetch()
        .stream()
        .filter(post -> dayType == 0 || isDayTypeIncluded(post.getDayType(), dayType))
        .map(StudyPostDto::fromEntity)
        .toList();
  }

  private BooleanExpression containsTitle(String title) {
    return title != null ? QStudyPost.studyPost.title.contains(title) : null;
  }

  private BooleanExpression equalsSubject(StudySubject subject) {
    return subject != null ? QStudyPost.studyPost.subject.eq(subject) : null;
  }

  private BooleanExpression equalsDifficulty(StudyDifficulty difficulty) {
    return difficulty != null ? QStudyPost.studyPost.difficulty.eq(difficulty) : null;
  }

  private BooleanExpression equalsStatus(StudyStatus status) {
    return status != null ? QStudyPost.studyPost.status.eq(status) : null;
  }

  private boolean isDayTypeIncluded(int postDayType, int filterDayType) {
    return (postDayType & filterDayType) == filterDayType;
  }
}