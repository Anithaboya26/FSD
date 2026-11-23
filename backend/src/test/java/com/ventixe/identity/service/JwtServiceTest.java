package com.ventixe.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ventixe.config.AuthProperties;
import com.ventixe.identity.entity.Role;
import com.ventixe.identity.entity.User;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);

  @Test
  void tokenIsSignedAndContainsAccountIdentityAndExpiration() {
    JwtService service = new JwtService(properties(SECRET, Duration.ofMinutes(15)));
    Role role = org.mockito.Mockito.mock(Role.class);
    when(role.getName()).thenReturn("ORGANIZER");
    User user = org.mockito.Mockito.mock(User.class);
    when(user.getId()).thenReturn(17L);
    when(user.getRole()).thenReturn(role);

    var claims = service.verify(service.issue(user));

    assertThat(claims.getSubject()).isEqualTo("17");
    assertThat(claims.getIssuer()).isEqualTo("ventixe-test");
    assertThat(claims.get("role", String.class)).isEqualTo("ORGANIZER");
    assertThat(claims.getExpiration()).isNotNull();
  }

  @Test
  void jwtSecretMustContainAtLeast32DecodedBytes() {
    String shortSecret = Base64.getEncoder().encodeToString(new byte[31]);

    assertThatThrownBy(() -> new JwtService(properties(shortSecret, Duration.ofMinutes(15))))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("JWT_SECRET must decode to at least 32 bytes");
  }

  @Test
  void jwtLifetimeMustBePositiveAndNoMoreThanOneHour() {
    assertThatThrownBy(() -> new JwtService(properties(SECRET, Duration.ZERO)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("JWT_TTL must be greater than zero and at most one hour");
  }

  private AuthProperties properties(String secret, Duration ttl) {
    return new AuthProperties(secret, "ventixe-test", ttl, false, List.of("http://localhost:5173"));
  }
}
