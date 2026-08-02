package com.hotelfo.scanner.config;

import com.hotelfo.scanner.entity.User;
import com.hotelfo.scanner.entity.enums.Role;
import com.hotelfo.scanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Membuat 1 akun admin default saat aplikasi start, HANYA jika profile "dev" aktif.
 * Memudahkan testing endpoint /auth/login tanpa harus insert manual ke MySQL.
 *
 * TIDAK aktif di production (tidak akan jalan tanpa -Dspring.profiles.active=dev).
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }

        User admin = User.builder()
                .username("admin")
                .email("admin@hotelfo.local")
                .passwordHash(passwordEncoder.encode("Admin@12345"))
                .fullName("Administrator")
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
        log.info(">>> Dev admin user seeded. username=admin | password=Admin@12345");
    }
}
