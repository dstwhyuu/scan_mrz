package com.hotelfo.scanner.controller;

import com.hotelfo.scanner.dto.request.CreateUserRequest;
import com.hotelfo.scanner.dto.response.UserResponse;
import com.hotelfo.scanner.entity.User;
import com.hotelfo.scanner.entity.enums.Role;
import com.hotelfo.scanner.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoint sederhana untuk kelola user Front Office.
 * Semua user yang sudah login bisa menambah, melihat, dan menonaktifkan user lain.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAllByDeletedAtIsNull().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.FRONT_OFFICE)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(false);
                    user.setDeletedAt(LocalDateTime.now());
                    userRepository.save(user);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .active(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
