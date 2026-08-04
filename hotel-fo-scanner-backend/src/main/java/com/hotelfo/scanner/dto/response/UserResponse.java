package com.hotelfo.scanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private boolean active;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
