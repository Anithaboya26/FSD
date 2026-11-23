package com.ventixe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final AuthProperties authProperties;

  public WebConfig(AuthProperties authProperties) {
    this.authProperties = authProperties;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins(authProperties.allowedOrigins().toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("Content-Type", "X-XSRF-TOKEN", "X-Requested-With")
        .allowCredentials(true)
        .maxAge(3600);
  }
}
