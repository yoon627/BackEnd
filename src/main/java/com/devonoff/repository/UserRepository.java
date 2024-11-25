package com.devonoff.repository;

import com.devonoff.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email); // 이메일로 사용자 찾기
  Optional<User> findByUsername(String username); // 닉네임으로 사용자 찾기
  boolean existsByEmail(String email); // 이메일 중복 확인
  boolean existsByUsername(String username); // 닉네임 중복 확인
}

