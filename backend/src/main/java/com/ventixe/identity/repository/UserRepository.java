package com.ventixe.identity.repository;

import com.ventixe.identity.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  @EntityGraph(attributePaths = "role")
  Optional<User> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  @Override
  @EntityGraph(attributePaths = "role")
  Optional<User> findById(Long id);
}
