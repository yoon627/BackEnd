package com.devonoff.domain.studyPost.repository;

import com.devonoff.domain.studyPost.dto.StudyPostDto;
import com.devonoff.domain.studyPost.entity.QStudyPost;
import com.devonoff.domain.studyPost.entity.StudyPost;
import com.devonoff.type.StudyDifficulty;
import com.devonoff.type.StudyMeetingType;
import com.devonoff.type.StudyPostStatus;
import com.devonoff.type.StudySubject;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyPostRepositoryCustomImpl implements StudyPostRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<StudyPostDto> findStudyPostsByFilters(
      StudyMeetingType meetingType, String title, StudySubject subject,
      StudyDifficulty difficulty, int dayType, StudyPostStatus status,
      Double latitude, Double longitude, Pageable pageable) {

    QStudyPost studyPost = QStudyPost.studyPost;

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(equalsMeetingType(meetingType));
    builder.and(containsTitle(title));
    builder.and(equalsSubject(subject));
    builder.and(equalsDifficulty(difficulty));
    builder.and(equalsStatus(status));
    builder.and(equalsDayType(dayType));

    JPAQuery<StudyPost> query = queryFactory
        .selectFrom(studyPost)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize());

    query = applySorting(query, meetingType, latitude, longitude, studyPost);

    List<StudyPostDto> results = query.fetch()
        .stream()
        .map(StudyPostDto::fromEntity)
        .toList();

    long total = queryFactory
        .selectFrom(studyPost)
        .where(builder)
        .fetchCount();

    return new PageImpl<>(results, pageable, total);
  }

  private BooleanBuilder equalsMeetingType(StudyMeetingType meetingType) {
    return meetingType != null ? new BooleanBuilder(
        QStudyPost.studyPost.meetingType.eq(meetingType)) : new BooleanBuilder();
  }

  private BooleanBuilder containsTitle(String title) {
    return title != null ? new BooleanBuilder(QStudyPost.studyPost.title.contains(title))
        : new BooleanBuilder();
  }

  private BooleanBuilder equalsSubject(StudySubject subject) {
    return subject != null ? new BooleanBuilder(QStudyPost.studyPost.subject.eq(subject))
        : new BooleanBuilder();
  }

  private BooleanBuilder equalsDifficulty(StudyDifficulty difficulty) {
    return difficulty != null ? new BooleanBuilder(QStudyPost.studyPost.difficulty.eq(difficulty))
        : new BooleanBuilder();
  }

  private BooleanBuilder equalsStatus(StudyPostStatus status) {
    return status != null ? new BooleanBuilder(QStudyPost.studyPost.status.eq(status))
        : new BooleanBuilder();
  }

  private BooleanBuilder equalsDayType(int filterDayType) {
    if (filterDayType == 0) {
      return new BooleanBuilder();
    }
    return new BooleanBuilder(
        Expressions.booleanTemplate("function('bitand', {0}, {1}) = {1}",
            QStudyPost.studyPost.dayType, filterDayType));
  }

  private JPAQuery<StudyPost> applySorting(
      JPAQuery<StudyPost> query,
      StudyMeetingType meetingType,
      Double latitude,
      Double longitude,
      QStudyPost studyPost) {
    if (StudyMeetingType.HYBRID.equals(meetingType) && latitude != null && longitude != null) {
      return query.orderBy(
          Expressions.numberTemplate(
              Double.class,
              "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({4})) * sin(radians({5})))",
              latitude,
              studyPost.latitude,
              studyPost.longitude,
              longitude,
              latitude,
              studyPost.latitude
          ).asc());
    }
    return query.orderBy(studyPost.createdAt.desc());
  }
}