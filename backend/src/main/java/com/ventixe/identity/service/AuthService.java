package com.ventixe.identity.service;

import com.ventixe.identity.dto.AuthResult;
import com.ventixe.identity.dto.CurrentUserResponse;
import com.ventixe.identity.dto.LoginRequest;
import com.ventixe.identity.dto.RegisterRequest;
import com.ventixe.identity.entity.Organizer;
import com.ventixe.identity.entity.Role;
import com.ventixe.identity.entity.User;
import com.ventixe.identity.entity.UserProfile;
import com.ventixe.identity.entity.UserStatus;
import com.ventixe.identity.exception.AccountUnavailableException;
import com.ventixe.identity.exception.EmailAlreadyRegisteredException;
import com.ventixe.identity.exception.InvalidCredentialsException;
import com.ventixe.identity.repository.OrganizerRepository;
import com.ventixe.identity.repository.RoleRepository;
import com.ventixe.identity.repository.UserProfileRepository;
import com.ventixe.identity.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserProfileRepository userProfileRepository;
  private final OrganizerRepository organizerRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      UserProfileRepository userProfileRepository,
      OrganizerRepository organizerRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.userProfileRepository = userProfileRepository;
    this.organizerRepository = organizerRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthResult register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new EmailAlreadyRegisteredException();
    }
    validatePasswordLength(request.password());

    String roleName = request.role();
    if (!"CUSTOMER".equals(roleName) && !"ORGANIZER".equals(roleName)) {
      throw new IllegalArgumentException("Only customer and organizer accounts can self-register");
    }
    if ("ORGANIZER".equals(roleName)
        && (request.businessName() == null || request.businessName().isBlank())) {
      throw new IllegalArgumentException("businessName is required for organizer registration");
    }
    Role role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new IllegalStateException("Required role is missing from the database"));

    User user = userRepository.saveAndFlush(
        new User(role, email, passwordEncoder.encode(request.password())));
    userProfileRepository.save(new UserProfile(user, request.firstName().trim(), request.lastName().trim()));
    if ("ORGANIZER".equals(roleName)) {
      organizerRepository.save(new Organizer(user, request.businessName().trim()));
    }
    return resultFor(user);
  }

  @Transactional(readOnly = true)
  public AuthResult login(LoginRequest request) {
    String email = normalizeEmail(request.email());
    if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new InvalidCredentialsException();
    }
    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(InvalidCredentialsException::new);
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new AccountUnavailableException();
    }
    return resultFor(user);
  }

  @Transactional(readOnly = true)
  public CurrentUserResponse currentUser(long userId) {
    User user = userRepository.findById(userId)
        .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
        .orElseThrow(InvalidCredentialsException::new);
    return toResponse(user);
  }

  private AuthResult resultFor(User user) {
    return new AuthResult(jwtService.issue(user), toResponse(user));
  }

  private CurrentUserResponse toResponse(User user) {
    return new CurrentUserResponse(user.getId(), user.getEmail(), user.getRole().getName());
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private void validatePasswordLength(String password) {
    if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new IllegalArgumentException("password must be at most 72 UTF-8 bytes");
    }
  }
}
