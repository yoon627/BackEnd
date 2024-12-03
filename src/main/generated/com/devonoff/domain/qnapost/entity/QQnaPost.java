package com.devonoff.domain.qnapost.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQnaPost is a Querydsl query type for QnaPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQnaPost extends EntityPathBase<QnaPost> {

    private static final long serialVersionUID = 2035535655L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQnaPost qnaPost = new QQnaPost("qnaPost");

    public final com.devonoff.common.entity.QBaseTimeEntity _super = new com.devonoff.common.entity.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.devonoff.type.PostType> postType = createEnum("postType", com.devonoff.type.PostType.class);

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.devonoff.domain.user.entity.QUser user;

    public QQnaPost(String variable) {
        this(QnaPost.class, forVariable(variable), INITS);
    }

    public QQnaPost(Path<? extends QnaPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQnaPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQnaPost(PathMetadata metadata, PathInits inits) {
        this(QnaPost.class, metadata, inits);
    }

    public QQnaPost(Class<? extends QnaPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.devonoff.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

