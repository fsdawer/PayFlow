package com.payflow.global.config;

import com.payflow.global.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final StringRedisTemplate redisTemplate;

  public JwtAuthFilter(JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
    this.jwtUtil = jwtUtil;
    this.redisTemplate = redisTemplate;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      if (jwtUtil.isTokenValid(token) && !isBlacklisted(token)) {
        // UserId를 Long으로 추출
        Long userId = jwtUtil.getUserId(token);
        // UserId를 principal로 설정
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, null);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }

  private boolean isBlacklisted(String token) {
    String key = "blacklist:" + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }
}
