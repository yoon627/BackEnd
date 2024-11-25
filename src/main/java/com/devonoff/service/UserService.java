package com.devonoff.service;


import com.devonoff.dto.SignUpRequest;

public interface UserService {
  // 회원가입
  void signUp(SignUpRequest request);


  }