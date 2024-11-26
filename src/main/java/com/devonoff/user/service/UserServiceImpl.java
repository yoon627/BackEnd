package com.devonoff.user.service;


import com.devonoff.user.dto.SignUpRequest;
import com.devonoff.user.entity.User;
import com.devonoff.user.repository.UserRepository;
import com.devonoff.user.type.LoginType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;



  // 회원가입 메서드 구현
  @Override
  public void signUp(SignUpRequest request) {
    // 이메일 중복 확인
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
    }

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // User 엔티티 생성 및 저장
    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(encodedPassword);
    user.setLoginType(LoginType.GENERAL);

    userRepository.save(user);
  }
}