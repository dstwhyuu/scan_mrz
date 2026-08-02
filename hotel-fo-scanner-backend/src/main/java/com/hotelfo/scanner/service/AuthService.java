package com.hotelfo.scanner.service;

import com.hotelfo.scanner.dto.request.LoginRequest;
import com.hotelfo.scanner.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(String refreshToken);
}
