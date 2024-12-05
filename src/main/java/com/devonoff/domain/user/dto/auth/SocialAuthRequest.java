package com.devonoff.domain.user.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SocialAuthRequest {

  @NotBlank
  private String code;

}
