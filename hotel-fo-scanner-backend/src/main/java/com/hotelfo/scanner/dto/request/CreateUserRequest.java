package com.hotelfo.scanner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 50, message = "Username harus antara 3-50 karakter")
    private String username;

    @NotBlank(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 8, max = 100, message = "Password minimal 8 karakter")
    private String password;

    @NotBlank(message = "Nama lengkap tidak boleh kosong")
    @Size(max = 100)
    private String fullName;
}
