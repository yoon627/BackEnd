package com.devonoff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
public class DevOnOffApplication {

  public static void main(String[] args) {
    SpringApplication.run(DevOnOffApplication.class, args);
  }

}
