package com.devonoff.studyPost.repository;

import com.devonoff.studyPost.entity.StudyPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {

}
