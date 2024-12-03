package com.devonoff.domain.studyPost.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStudyPost is a Querydsl query type for StudyPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStudyPost extends EntityPathBase<StudyPost> {

    private static final long serialVersionUID = -1368985625L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStudyPost studyPost = new QStudyPost("studyPost");

    public final com.devonoff.common.entity.QBaseTimeEntity _super = new com.devonoff.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> dayType = createNumber("dayType", Integer.class);

    public final StringPath description = createString("description");

    public final EnumPath<com.devonoff.type.StudyDifficulty> difficulty = createEnum("difficulty", com.devonoff.type.StudyDifficulty.class);

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> endTime = createTime("endTime", java.time.LocalTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final EnumPath<com.devonoff.type.StudyMeetingType> meetingType = createEnum("meetingType", com.devonoff.type.StudyMeetingType.class);

    public final DatePath<java.time.LocalDate> recruitmentPeriod = createDate("recruitmentPeriod", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public final EnumPath<com.devonoff.type.StudyStatus> status = createEnum("status", com.devonoff.type.StudyStatus.class);

    public final StringPath studyName = createString("studyName");

    public final EnumPath<com.devonoff.type.StudySubject> subject = createEnum("subject", com.devonoff.type.StudySubject.class);

    public final StringPath thumbnailImgUrl = createString("thumbnailImgUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.devonoff.domain.user.entity.QUser user;

    public QStudyPost(String variable) {
        this(StudyPost.class, forVariable(variable), INITS);
    }

    public QStudyPost(Path<? extends StudyPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStudyPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStudyPost(PathMetadata metadata, PathInits inits) {
        this(StudyPost.class, metadata, inits);
    }

    public QStudyPost(Class<? extends StudyPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.devonoff.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

