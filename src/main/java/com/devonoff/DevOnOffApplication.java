package com.devonoff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.devonoff")
public class DevOnOffApplication {

  public static void main(String[] args) {
    SpringApplication.run(DevOnOffApplication.class, args);
  }

}
