package com.payflow.domain.user.service;

import com.payflow.domain.user.dto.LoginRequest;
import com.payflow.domain.user.dto.LoginResponse;
import com.payflow.domain.user.dto.SignUpRequest;
import com.payflow.domain.user.dto.SignUpResponse;
import com.payflow.domain.user.entity.User;
import com.payflow.domain.user.repository.UserRepository;
import com.payflow.global.util.JwtUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final StringRedisTemplate redisTemplate;


  // 회원가입
  @Override
  public SignUpResponse signUp(SignUpRequest request) { // SignUpRequest에서 받은 값들을 파라미터로 받음
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
    }

    User user =
        User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .createdAt(LocalDateTime.now())
            .build();

    User saved = userRepository.save(user);
    return new SignUpResponse(saved.getUserId(), saved.getEmail(), saved.getName());
  }


// 로그인
  @Override
  public LoginResponse login(LoginRequest request) {
    User user = userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    String token = jwtUtil.generateAccessToken(user.getUserId(), user.getEmail());
    return new LoginResponse(token);
  }


  // 로그아웃
  @Override
  public void logout(String accessToken) {
    if (accessToken == null || accessToken.isBlank()) {
      throw new IllegalArgumentException("토큰이 필요합니다.");
    }

    Date expiry = jwtUtil.getExpiration(accessToken);
    long remainingMs = expiry.getTime() - System.currentTimeMillis();
    if (remainingMs > 0) {
      String key = "blacklist:" + accessToken;
      redisTemplate.opsForValue().set(key, "logout", Duration.ofMillis(remainingMs));
    }
  }
}
