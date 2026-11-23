package com.ventixe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VentixeApplication {

  public static void main(String[] args) {
    SpringApplication.run(VentixeApplication.class, args);
  }
}
