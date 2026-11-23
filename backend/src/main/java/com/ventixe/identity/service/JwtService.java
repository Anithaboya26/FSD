package com.ventixe.identity.service;

import com.ventixe.config.AuthProperties;
import com.ventixe.identity.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final AuthProperties properties;
  private final SecretKey signingKey;

  public JwtService(AuthProperties properties) {
    this.properties = properties;
    if (properties.jwtTtl() == null
        || properties.jwtTtl().isNegative()
        || properties.jwtTtl().isZero()
        || properties.jwtTtl().compareTo(java.time.Duration.ofHours(1)) > 0) {
      throw new IllegalStateException("JWT_TTL must be greater than zero and at most one hour");
    }
    byte[] keyBytes;
    try {
      keyBytes = java.util.Base64.getDecoder().decode(properties.jwtSecret());
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException("JWT_SECRET must be valid Base64", exception);
    }
    if (keyBytes.length < 32) {
      throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes");
    }
    this.signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
  }

  public String issue(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(properties.jwtIssuer())
        .subject(user.getId().toString())
        .claim("role", user.getRole().getName())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(properties.jwtTtl())))
        .signWith(signingKey)
        .compact();
  }

  public Claims verify(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .requireIssuer(properties.jwtIssuer())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public long expiresInSeconds() {
    return properties.jwtTtl().toSeconds();
  }
}
