package com.ventixe.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ventixe.config.AuthProperties;
import com.ventixe.identity.entity.Role;
import com.ventixe.identity.entity.User;
import com.ventixe.identity.entity.UserStatus;
import com.ventixe.identity.repository.UserRepository;
import com.ventixe.identity.service.JwtService;
import jakarta.servlet.FilterChain;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);
  private static final AuthProperties PROPERTIES =
      new AuthProperties(SECRET, "ventixe-test", Duration.ofMinutes(15), false, List.of());

  private UserRepository userRepository;
  private JwtService jwtService;
  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    userRepository = org.mockito.Mockito.mock(UserRepository.class);
    jwtService = new JwtService(PROPERTIES);
    filter = new JwtAuthenticationFilter(jwtService, userRepository, PROPERTIES);
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void validTokenLoadsTheCurrentDatabaseRole() throws Exception {
    User user = user(73L, "ORGANIZER", UserStatus.ACTIVE);
    when(user.getEmail()).thenReturn("organizer@example.com");
    when(userRepository.findById(73L)).thenReturn(Optional.of(user));
    String token = jwtService.issue(user);
    MockHttpServletRequest request = requestWithToken(token);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (filteredRequest, filteredResponse) -> {
    };

    filter.doFilterInternal(request, response, chain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getPrincipal()).isEqualTo(new UserPrincipal(73L, "organizer@example.com", "ORGANIZER"));
    assertThat(authentication.getAuthorities())
        .extracting(authority -> authority.getAuthority())
        .containsExactly("ROLE_ORGANIZER");
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void suspendedAccountIsNotAuthenticatedAndItsCookieIsExpired() throws Exception {
    User user = user(73L, "CUSTOMER", UserStatus.SUSPENDED);
    when(userRepository.findById(73L)).thenReturn(Optional.of(user));
    MockHttpServletRequest request = requestWithToken(jwtService.issue(user));
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (filteredRequest, filteredResponse) -> {
    };

    filter.doFilterInternal(request, response, chain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(response.getHeader("Set-Cookie"))
        .contains("VENTIXE_AUTH=")
        .contains("Max-Age=0");
  }

  private MockHttpServletRequest requestWithToken(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new jakarta.servlet.http.Cookie(
        JwtAuthenticationFilter.AUTH_COOKIE_NAME,
        token));
    return request;
  }

  private User user(long id, String roleName, UserStatus status) {
    Role role = org.mockito.Mockito.mock(Role.class);
    when(role.getName()).thenReturn(roleName);
    User user = org.mockito.Mockito.mock(User.class);
    when(user.getId()).thenReturn(id);
    when(user.getRole()).thenReturn(role);
    when(user.getStatus()).thenReturn(status);
    return user;
  }
}
