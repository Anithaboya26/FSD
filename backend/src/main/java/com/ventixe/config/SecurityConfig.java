package com.ventixe.config;

import com.ventixe.security.JwtAuthenticationFilter;
import com.ventixe.security.SpaCsrfTokenRequestHandler;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

  @Bean
  FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
      JwtAuthenticationFilter filter) {
    FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      AuthProperties authProperties) throws Exception {
    CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfRepository.setCookieCustomizer(cookie -> cookie
        .sameSite(authProperties.secureCookie() ? "None" : "Lax")
        .secure(authProperties.secureCookie())
        .path("/"));
    AuthenticationEntryPoint authenticationEntryPoint = (request, response, exception) -> {
      response.setStatus(401);
      response.setContentType("application/json");
      response.getWriter().write(
          "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required\"}");
    };
    AccessDeniedHandler accessDeniedHandler = (request, response, exception) -> {
      response.setStatus(403);
      response.setContentType("application/json");
      response.getWriter().write(
          "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You are not allowed to perform this action\"}");
    };

    http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfRepository)
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(errors -> errors
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/actuator/health/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf", "/api/v1/auth/me").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/logout").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/categories", "/api/v1/events", "/api/v1/events/**").permitAll()
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/organizer/**").hasRole("ORGANIZER")
            .requestMatchers("/api/v1/customer/**").hasRole("CUSTOMER")
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(AuthProperties authProperties) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(authProperties.allowedOrigins());
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "X-Requested-With"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(Duration.ofHours(1));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}
