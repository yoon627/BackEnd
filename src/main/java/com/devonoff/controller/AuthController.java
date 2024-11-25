package com.devonoff.controller;

import com.devonoff.dto.SignUpRequest;
import com.devonoff.service.UserService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;



  /**
   * 회원가입
   */
  @PostMapping("/sign-up")
  public ResponseEntity<Map<String, Object>> signUp(@RequestBody SignUpRequest request) {
    userService.signUp(request);
    // 성공 응답 생성
    Map<String, Object> response = new HashMap<>();
    response.put("message", "회원가입에 성공하였습니다.");

    return ResponseEntity.status(201).body(response);
  }

}