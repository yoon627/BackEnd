package com.devonoff.domain.totalstudytime.repository;

import com.devonoff.domain.totalstudytime.entity.TotalStudyTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TotalStudyTimeRepository extends JpaRepository<TotalStudyTime, Long> {

  List<TotalStudyTime> findTop10ByOrderByTotalStudyTimeDesc();

  List<TotalStudyTime> findAllByOrderByTotalStudyTimeDesc();
}
