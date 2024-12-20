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
import java.util.Optional;
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

    long total = Optional.ofNullable(queryFactory
            .select(studyPost.count())
            .from(studyPost)
            .where(builder)
            .fetchOne())
        .orElse(0L);

    return new PageImpl<>(results, pageable, total);
  }

  private BooleanBuilder equalsMeetingType(StudyMeetingType meetingType) {
    return Optional.ofNullable(meetingType)
        .map(mt -> new BooleanBuilder(QStudyPost.studyPost.meetingType.eq(mt)))
        .orElseGet(BooleanBuilder::new);
  }

  private BooleanBuilder containsTitle(String title) {
    return Optional.ofNullable(title)
        .map(t -> new BooleanBuilder(QStudyPost.studyPost.title.contains(t)))
        .orElseGet(BooleanBuilder::new);
  }

  private BooleanBuilder equalsSubject(StudySubject subject) {
    return Optional.ofNullable(subject)
        .map(s -> new BooleanBuilder(QStudyPost.studyPost.subject.eq(s)))
        .orElseGet(BooleanBuilder::new);
  }

  private BooleanBuilder equalsDifficulty(StudyDifficulty difficulty) {
    return Optional.ofNullable(difficulty)
        .map(d -> new BooleanBuilder(QStudyPost.studyPost.difficulty.eq(d)))
        .orElseGet(BooleanBuilder::new);
  }

  private BooleanBuilder equalsStatus(StudyPostStatus status) {
    return Optional.ofNullable(status)
        .map(s -> new BooleanBuilder(QStudyPost.studyPost.status.eq(s)))
        .orElseGet(BooleanBuilder::new);
  }

  private BooleanBuilder equalsDayType(int filterDayType) {
    if (filterDayType == 0) {
      return new BooleanBuilder();
    }
    // 비트 플래그 연산을 사용하여 dayType 필터 조건 확인
    return new BooleanBuilder(
        Expressions.booleanTemplate(
            "function('bitand', {0}, {1}) = {1}",
            QStudyPost.studyPost.dayType, filterDayType));
  }

  private JPAQuery<StudyPost> applySorting(
      JPAQuery<StudyPost> query,
      StudyMeetingType meetingType,
      Double latitude,
      Double longitude,
      QStudyPost studyPost) {

    if (StudyMeetingType.HYBRID.equals(meetingType) && latitude != null && longitude != null) {
      var distanceExpression = Expressions.numberTemplate(
          Double.class,
          "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({4})) * sin(radians({5})))",
          latitude, studyPost.latitude, studyPost.longitude, longitude, latitude, studyPost.latitude
      );
      return query.orderBy(distanceExpression.asc());
    } else {
      return query.orderBy(studyPost.createdAt.desc());
    }
  }
}