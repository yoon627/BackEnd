package com.devonoff.domain.videoconference.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

  @GetMapping("/healthcheck")
  public String healthcheck() {
    return "ok";
  }
}
