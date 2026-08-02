package com.hotelfo.scanner.service.impl;

import com.hotelfo.scanner.dto.request.LoginRequest;
import com.hotelfo.scanner.dto.response.LoginResponse;
import com.hotelfo.scanner.dto.response.UserSummaryResponse;
import com.hotelfo.scanner.entity.User;
import com.hotelfo.scanner.repository.UserRepository;
import com.hotelfo.scanner.security.CustomUserPrincipal;
import com.hotelfo.scanner.security.JwtTokenProvider;
import com.hotelfo.scanner.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final short MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Username atau password salah"));

        if (user.isLocked()) {
            throw new LockedException("Akun terkunci sementara karena terlalu banyak percobaan gagal. Coba lagi nanti.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Login sukses: reset counter percobaan gagal & catat waktu login
            user.setFailedLoginAttempts((short) 0);
            user.setLockedUntil(null);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            return buildLoginResponse(principal);

        } catch (BadCredentialsException ex) {
            registerFailedAttempt(user);
            throw ex;
        }
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.isTokenValid(refreshToken)
                || !"refresh".equals(jwtTokenProvider.extractTokenType(refreshToken))) {
            throw new BadCredentialsException("Refresh token tidak valid atau sudah kedaluwarsa");
        }

        String username = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BadCredentialsException("User tidak ditemukan"));

        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        return buildLoginResponse(principal);
    }

    private void registerFailedAttempt(User user) {
        short attempts = (short) (user.getFailedLoginAttempts() + 1);
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        userRepository.save(user);
    }

    private LoginResponse buildLoginResponse(CustomUserPrincipal principal) {
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(principal);

        UserSummaryResponse userSummary = UserSummaryResponse.builder()
                .id(principal.getId())
                .username(principal.getUsername())
                .fullName(principal.getUser().getFullName())
                .role(principal.getUser().getRole().name())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(userSummary)
                .build();
    }
}
