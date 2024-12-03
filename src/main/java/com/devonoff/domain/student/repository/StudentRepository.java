package com.devonoff.domain.student.repository;

import com.devonoff.domain.student.entity.Student;
import com.devonoff.domain.study.entity.Study;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

  List<Student> findByStudy(Study study);
}
