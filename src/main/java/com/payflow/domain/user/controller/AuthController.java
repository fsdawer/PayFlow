package com.payflow.domain.user.controller;

import com.payflow.domain.user.dto.LoginRequest;
import com.payflow.domain.user.dto.LoginResponse;
import com.payflow.domain.user.dto.SignUpRequest;
import com.payflow.domain.user.dto.SignUpResponse;
import com.payflow.domain.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }


  @PostMapping("/signup")
  public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
    return ResponseEntity.ok(authService.signUp(request));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
    String token = extractToken(authHeader);
    authService.logout(token);
    return ResponseEntity.ok().build();
  }


  private String extractToken(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("유효한 Authorization 헤더가 필요합니다.");
    }
    return authHeader.substring(7);
  }
}
