package com.ventixe.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ventixe.identity.dto.LoginRequest;
import com.ventixe.identity.dto.RegisterRequest;
import com.ventixe.identity.entity.Role;
import com.ventixe.identity.entity.User;
import com.ventixe.identity.entity.UserStatus;
import com.ventixe.identity.exception.AccountUnavailableException;
import com.ventixe.identity.exception.InvalidCredentialsException;
import com.ventixe.identity.repository.OrganizerRepository;
import com.ventixe.identity.repository.RoleRepository;
import com.ventixe.identity.repository.UserProfileRepository;
import com.ventixe.identity.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private UserProfileRepository userProfileRepository;

  @Mock
  private OrganizerRepository organizerRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(
        userRepository,
        roleRepository,
        userProfileRepository,
        organizerRepository,
        passwordEncoder,
        jwtService);
  }

  @Test
  void loginNormalizesEmailAndReturnsRoleWithoutReturningTokenInUser() {
    User user = userWithRole("CUSTOMER");
    when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
    when(user.getPasswordHash()).thenReturn("bcrypt-hash");
    when(userRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("a-long-password", "bcrypt-hash")).thenReturn(true);
    when(jwtService.issue(user)).thenReturn("signed-jwt");
    when(user.getId()).thenReturn(41L);
    when(user.getEmail()).thenReturn("person@example.com");

    var result = authService.login(new LoginRequest(" PERSON@EXAMPLE.COM ", "a-long-password"));

    assertThat(result.token()).isEqualTo("signed-jwt");
    assertThat(result.user().email()).isEqualTo("person@example.com");
    assertThat(result.user().role()).isEqualTo("CUSTOMER");
  }

  @Test
  void loginRejectsIncorrectPassword() {
    User user = org.mockito.Mockito.mock(User.class);
    when(userRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(user));
    when(user.getPasswordHash()).thenReturn("bcrypt-hash");
    when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

    assertThatThrownBy(() -> authService.login(
        new LoginRequest("person@example.com", "wrong-password")))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void loginRejectsInactiveAccounts() {
    User user = org.mockito.Mockito.mock(User.class);
    when(userRepository.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(user));
    when(user.getPasswordHash()).thenReturn("bcrypt-hash");
    when(passwordEncoder.matches("a-long-password", "bcrypt-hash")).thenReturn(true);
    when(user.getStatus()).thenReturn(UserStatus.SUSPENDED);

    assertThatThrownBy(() -> authService.login(
        new LoginRequest("person@example.com", "a-long-password")))
        .isInstanceOf(AccountUnavailableException.class);
  }

  @Test
  void registrationDoesNotAllowSelfAssigningAdminRole() {
    RegisterRequest request = new RegisterRequest(
        "person@example.com",
        "a-long-password",
        "First",
        "Last",
        "ADMIN",
        null);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Only customer and organizer accounts can self-register");
    verify(roleRepository, never()).findByName(any());
  }

  @Test
  void registrationCreatesCustomerProfileAndUsesPasswordHash() {
    Role role = org.mockito.Mockito.mock(Role.class);
    User user = userWithRole("CUSTOMER");
    when(userRepository.existsByEmailIgnoreCase("person@example.com")).thenReturn(false);
    when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(role));
    when(passwordEncoder.encode("a-long-password")).thenReturn("new-bcrypt-hash");
    when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);
    when(user.getId()).thenReturn(41L);
    when(user.getEmail()).thenReturn("person@example.com");
    when(jwtService.issue(user)).thenReturn("signed-jwt");

    var result = authService.register(new RegisterRequest(
        " PERSON@EXAMPLE.COM ",
        "a-long-password",
        "  First ",
        " Last  ",
        "CUSTOMER",
        null));

    assertThat(result.user().role()).isEqualTo("CUSTOMER");
    verify(userRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(
        saved -> "person@example.com".equals(saved.getEmail())
            && "new-bcrypt-hash".equals(saved.getPasswordHash())));
    verify(userProfileRepository).save(org.mockito.ArgumentMatchers.argThat(
        profile -> profile != null));
    verify(organizerRepository, never()).save(any());
  }

  private User userWithRole(String roleName) {
    Role role = org.mockito.Mockito.mock(Role.class);
    when(role.getName()).thenReturn(roleName);
    User user = org.mockito.Mockito.mock(User.class);
    when(user.getRole()).thenReturn(role);
    return user;
  }
}
