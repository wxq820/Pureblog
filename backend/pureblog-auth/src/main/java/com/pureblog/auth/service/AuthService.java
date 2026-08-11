package com.pureblog.auth.service;

import com.pureblog.auth.dto.*;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    void logout();

    AuthResponse refreshToken(RefreshTokenRequest request);

    String generateCaptcha();

    boolean verifyCaptcha(String key, String code);
}
