package com.devonoff.domain.user.service.social;

import com.devonoff.type.LoginType;
import java.util.Map;

public interface SocialAuthProviderService {

  String getAccessToken(String code);

  Map<String, Object> getUserInfo(String accessToken);

  LoginType getLoginType();
}
