package com.payflow.domain.user.service;

import com.payflow.domain.user.dto.LoginRequest;
import com.payflow.domain.user.dto.LoginResponse;
import com.payflow.domain.user.dto.SignUpRequest;
import com.payflow.domain.user.dto.SignUpResponse;

public interface AuthService {

  SignUpResponse signUp(SignUpRequest request);

  LoginResponse login(LoginRequest request);

  void logout(String accessToken);
}
