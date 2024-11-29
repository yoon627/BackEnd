package com.devonoff.domain.user.repository;

import com.devonoff.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByNickname(String nickName);

  boolean existsByEmail(String email);

}

