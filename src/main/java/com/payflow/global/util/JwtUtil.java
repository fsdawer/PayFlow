package com.payflow.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private final SecretKey key;
  private final long accessTokenValidityMs;

  public JwtUtil(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-validity-ms}") long accessTokenValidityMs) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.accessTokenValidityMs = accessTokenValidityMs;
  }

  public String generateAccessToken(Long userId, String email) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenValidityMs);
    return Jwts.builder()
        .setSubject(email)
        .claim("uid", userId)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public boolean isTokenValid(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getEmail(String token) {
    return getClaims(token).getSubject();
  }

  public Long getUserId(String token) {
    return getClaims(token).get("uid", Long.class);
  }

  public Date getExpiration(String token) {
    return getClaims(token).getExpiration();
  }

  private Claims getClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
