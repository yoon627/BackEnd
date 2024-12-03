package com.devonoff.domain.study.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStudy is a Querydsl query type for Study
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStudy extends EntityPathBase<Study> {

    private static final long serialVersionUID = -2062661401L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStudy study = new QStudy("study");

    public final com.devonoff.common.entity.QBaseTimeEntity _super = new com.devonoff.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> dayType = createNumber("dayType", Integer.class);

    public final EnumPath<com.devonoff.type.StudyDifficulty> difficulty = createEnum("difficulty", com.devonoff.type.StudyDifficulty.class);

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.devonoff.type.StudyMeetingType> meetingType = createEnum("meetingType", com.devonoff.type.StudyMeetingType.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public final EnumPath<com.devonoff.type.StudyStatus> status = createEnum("status", com.devonoff.type.StudyStatus.class);

    public final com.devonoff.domain.user.entity.QUser studyLeader;

    public final StringPath studyName = createString("studyName");

    public final com.devonoff.domain.studyPost.entity.QStudyPost studyPost;

    public final EnumPath<com.devonoff.type.StudySubject> subject = createEnum("subject", com.devonoff.type.StudySubject.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStudy(String variable) {
        this(Study.class, forVariable(variable), INITS);
    }

    public QStudy(Path<? extends Study> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStudy(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStudy(PathMetadata metadata, PathInits inits) {
        this(Study.class, metadata, inits);
    }

    public QStudy(Class<? extends Study> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.studyLeader = inits.isInitialized("studyLeader") ? new com.devonoff.domain.user.entity.QUser(forProperty("studyLeader")) : null;
        this.studyPost = inits.isInitialized("studyPost") ? new com.devonoff.domain.studyPost.entity.QStudyPost(forProperty("studyPost"), inits.get("studyPost")) : null;
    }

}

