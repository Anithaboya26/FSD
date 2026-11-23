package com.ventixe.security;

import com.ventixe.config.AuthProperties;
import com.ventixe.identity.entity.User;
import com.ventixe.identity.entity.UserStatus;
import com.ventixe.identity.repository.UserRepository;
import com.ventixe.identity.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String AUTH_COOKIE_NAME = "VENTIXE_AUTH";

  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final AuthProperties authProperties;

  public JwtAuthenticationFilter(
      JwtService jwtService,
      UserRepository userRepository,
      AuthProperties authProperties) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.authProperties = authProperties;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String token = authCookie(request);
    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }

    Claims claims;
    long userId;
    String tokenRole;
    try {
      claims = jwtService.verify(token);
      userId = Long.parseLong(claims.getSubject());
      tokenRole = claims.get("role", String.class);
    } catch (JwtException | NumberFormatException exception) {
      SecurityContextHolder.clearContext();
      response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie().toString());
      filterChain.doFilter(request, response);
      return;
    }

    User user = userRepository.findById(userId)
        .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
        .orElse(null);
    String role = user == null ? null : user.getRole().getName();
    if (user == null || !role.equals(tokenRole)) {
      SecurityContextHolder.clearContext();
      response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie().toString());
      filterChain.doFilter(request, response);
      return;
    }

    UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), role);
    var authentication = UsernamePasswordAuthenticationToken.authenticated(
        principal,
        null,
        List.of(new SimpleGrantedAuthority("ROLE_" + principal.role())));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }

  private String authCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(cookie -> AUTH_COOKIE_NAME.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }

  private org.springframework.http.ResponseCookie expiredCookie() {
    return org.springframework.http.ResponseCookie.from(AUTH_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(authProperties.secureCookie())
        .sameSite(authProperties.secureCookie() ? "None" : "Lax")
        .path("/")
        .maxAge(0)
        .build();
  }

}
