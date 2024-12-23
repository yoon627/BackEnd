package com.devonoff.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/signaling")
//        .setAllowedOriginPatterns("*")
        .setAllowedOrigins("https://devonoff-develop-test.vercel.app",
            "https://devonoff-test.vercel.app", "https://devonoff-topaz.vercel.app")
//        .setAllowedOrigins("*")
        .withSockJS();

    registry.addEndpoint("/ws")
//        .setAllowedOrigins("https://devonoff-develop-test.vercel.app", "https://doanything.shop")
//        .setAllowedOrigins("*")
        .setAllowedOrigins("https://devonoff-develop-test.vercel.app",
            "https://devonoff-test.vercel.app", "https://devonoff-topaz.vercel.app")
        .withSockJS();
  }

}