package com.devonoff.domain.user.controller;

import com.devonoff.domain.user.dto.UserDto;
import com.devonoff.domain.user.dto.UserUpdateRequest;
import com.devonoff.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * 유저 정보 조회
   *
   * @param userId
   * @return ResponseEntity<UserDto>
   */
  @GetMapping
  public ResponseEntity<UserDto> getUserDetail(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.getUserDetail(userId));
  }

  /**
   * 유저 정보 수정
   *
   * @param userId
   * @param userUpdateRequest
   * @return ResponseEntity<UserDto>
   */
  @PutMapping
  public ResponseEntity<UserDto> updateUserInfo(
      @PathVariable Long userId,
      @RequestBody @Valid UserUpdateRequest userUpdateRequest
  ) {
    return ResponseEntity.ok(userService.updateUserInfo(userId, userUpdateRequest));
  }

  /**
   * 프로필 이미지 수정
   *
   * @param userId
   * @param profileImage
   * @return ResponseEntity<UserDto>
   */
  @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> updateProfileImage(
      @PathVariable Long userId,
      @RequestParam MultipartFile profileImage
  ) {
    return ResponseEntity.ok(userService.updateProfileImage(userId, profileImage));
  }

  /**
   * 프로필 이미지 삭제
   *
   * @param userId
   * @return ResponseEntity<UserDto>
   */
  @DeleteMapping("/profile-image")
  public ResponseEntity<UserDto> deleteProfileImage(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.deleteProfileImage(userId));
  }

}
