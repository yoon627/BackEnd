package com.devonoff.user.service;


import com.devonoff.user.dto.SignUpRequest;

public interface UserService {
  // 회원가입
  void signUp(SignUpRequest request);


  }