package com.devonoff.domain.infosharepost.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInfoSharePost is a Querydsl query type for InfoSharePost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInfoSharePost extends EntityPathBase<InfoSharePost> {

    private static final long serialVersionUID = 291526631L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInfoSharePost infoSharePost = new QInfoSharePost("infoSharePost");

    public final com.devonoff.common.entity.QBaseTimeEntity _super = new com.devonoff.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath thumbnailImgUrl = createString("thumbnailImgUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.devonoff.domain.user.entity.QUser user;

    public QInfoSharePost(String variable) {
        this(InfoSharePost.class, forVariable(variable), INITS);
    }

    public QInfoSharePost(Path<? extends InfoSharePost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInfoSharePost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInfoSharePost(PathMetadata metadata, PathInits inits) {
        this(InfoSharePost.class, metadata, inits);
    }

    public QInfoSharePost(Class<? extends InfoSharePost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.devonoff.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

