package com.devonoff.domain.student.repository;

import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.study.entity.Study;
import com.devonoff.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

  Boolean existsByUserIdAndStudyId(Long userId, Long studyId);

  List<Student> findByStudy(Study study);

  List<Student> findByUser(User user);

  int countParticipantsByStudy(Study study);
}
