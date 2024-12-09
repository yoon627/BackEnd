package com.devonoff.domain.user.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String nickname;

  @NotBlank
  @NotBlank
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "비밀번호는 최소 8자리 이상이며, 하나 이상의 영문자, 숫자, 특수문자를 포함해야 합니다."
  )
  private String password;

}
