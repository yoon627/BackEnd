package com.devonoff.domain.studyTimeline.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStudyTimeline is a Querydsl query type for StudyTimeline
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStudyTimeline extends EntityPathBase<StudyTimeline> {

    private static final long serialVersionUID = -1653203577L;

    public static final QStudyTimeline studyTimeline = new QStudyTimeline("studyTimeline");

    public final DateTimePath<java.time.LocalDateTime> endedAt = createDateTime("endedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> studyId = createNumber("studyId", Long.class);

    public QStudyTimeline(String variable) {
        super(StudyTimeline.class, forVariable(variable));
    }

    public QStudyTimeline(Path<? extends StudyTimeline> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStudyTimeline(PathMetadata metadata) {
        super(StudyTimeline.class, metadata);
    }

}

