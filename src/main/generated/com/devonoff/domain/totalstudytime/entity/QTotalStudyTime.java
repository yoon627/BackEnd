package com.devonoff.domain.totalstudytime.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTotalStudyTime is a Querydsl query type for TotalStudyTime
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTotalStudyTime extends EntityPathBase<TotalStudyTime> {

    private static final long serialVersionUID = 608462661L;

    public static final QTotalStudyTime totalStudyTime1 = new QTotalStudyTime("totalStudyTime1");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> studyId = createNumber("studyId", Long.class);

    public final NumberPath<Long> totalStudyTime = createNumber("totalStudyTime", Long.class);

    public QTotalStudyTime(String variable) {
        super(TotalStudyTime.class, forVariable(variable));
    }

    public QTotalStudyTime(Path<? extends TotalStudyTime> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTotalStudyTime(PathMetadata metadata) {
        super(TotalStudyTime.class, metadata);
    }

}

