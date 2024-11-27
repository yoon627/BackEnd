package com.devonoff.studyPost.repository;

import com.devonoff.studyPost.dto.StudyPostDto;
import com.devonoff.studyPost.entity.QStudyPost;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyStatus;
import com.devonoff.type.StudySubject;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyPostRepositoryCustomImpl implements StudyPostRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<StudyPostDto> findStudyPostsByFilters(
      StudyMeetingType meetingType, String title, StudySubject subject,
      StudyDifficulty difficulty, int dayType, StudyStatus status,
      Double latitude, Double longitude, Pageable pageable) {

    QStudyPost studyPost = QStudyPost.studyPost;

    var query = queryFactory
        .selectFrom(studyPost)
        .where(
            equalsMeetingType(meetingType),
            containsTitle(title),
            equalsSubject(subject),
            equalsDifficulty(difficulty),
            equalsStatus(status)
        )
        .orderBy(studyPost.createdAt.desc());

    if (StudyMeetingType.HYBRID.equals(meetingType) && latitude != null && longitude != null) {
      query = query.orderBy(
          Expressions.numberTemplate(Double.class,
              "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({4})) * sin(radians({5})))",
              latitude,
              studyPost.latitude,
              studyPost.longitude,
              longitude,
              latitude,
              studyPost.latitude
          ).asc()
      );
    }

    return query
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch().stream()
        .filter(post -> dayType == 0 || isDayTypeIncluded(post.getDayType(), dayType))
        .map(StudyPostDto::fromEntity)
        .toList();
  }

  private BooleanExpression equalsMeetingType(StudyMeetingType meetingType) {
    return meetingType != null ? QStudyPost.studyPost.meetingType.eq(meetingType) : null;
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