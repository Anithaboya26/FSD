package com.ventixe.identity.controller;

import com.ventixe.config.AuthProperties;
import com.ventixe.identity.dto.AuthResult;
import com.ventixe.identity.dto.CurrentUserResponse;
import com.ventixe.identity.dto.CsrfTokenResponse;
import com.ventixe.identity.dto.LoginRequest;
import com.ventixe.identity.dto.RegisterRequest;
import com.ventixe.identity.exception.InvalidCredentialsException;
import com.ventixe.identity.service.AuthService;
import com.ventixe.security.JwtAuthenticationFilter;
import com.ventixe.security.UserPrincipal;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final AuthProperties authProperties;

  public AuthController(AuthService authService, AuthProperties authProperties) {
    this.authService = authService;
    this.authProperties = authProperties;
  }

  @GetMapping("/csrf")
  public CsrfTokenResponse csrf(CsrfToken csrfToken) {
    return new CsrfTokenResponse(csrfToken.getToken());
  }

  @PostMapping("/register")
  public ResponseEntity<CurrentUserResponse> register(@Valid @RequestBody RegisterRequest request) {
    return authenticate(authService.register(request), 201);
  }

  @PostMapping("/login")
  public ResponseEntity<CurrentUserResponse> login(@Valid @RequestBody LoginRequest request) {
    return authenticate(authService.login(request), 200);
  }

  @GetMapping("/me")
  public CurrentUserResponse currentUser(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
      throw new InvalidCredentialsException();
    }
    return authService.currentUser(principal.id());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(authProperties.secureCookie())
        .sameSite(authProperties.secureCookie() ? "None" : "Lax")
        .path("/")
        .maxAge(Duration.ZERO)
        .build();
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .build();
  }

  private ResponseEntity<CurrentUserResponse> authenticate(AuthResult result, int status) {
    ResponseCookie cookie = ResponseCookie.from(
            JwtAuthenticationFilter.AUTH_COOKIE_NAME,
            result.token())
        .httpOnly(true)
        .secure(authProperties.secureCookie())
        .sameSite(authProperties.secureCookie() ? "None" : "Lax")
        .path("/")
        .maxAge(Duration.ofSeconds(authProperties.jwtTtl().toSeconds()))
        .build();
    return ResponseEntity.status(status)
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(result.user());
  }
}
