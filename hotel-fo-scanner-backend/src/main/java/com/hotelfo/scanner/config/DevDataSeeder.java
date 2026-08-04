package com.hotelfo.scanner.config;

import com.hotelfo.scanner.entity.User;
import com.hotelfo.scanner.entity.enums.Role;
import com.hotelfo.scanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Membuat 1 akun Front Office default saat pertama kali aplikasi start
 * dan database masih kosong (belum ada user sama sekali).
 *
 * Setelah user pertama dibuat, user tersebut bisa menambah user baru
 * melalui endpoint /api/v1/users.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User defaultUser = User.builder()
                .username("frontoffice")
                .email("frontoffice@hotel.local")
                .passwordHash(passwordEncoder.encode("FrontOffice@12345"))
                .fullName("Front Office")
                .role(Role.FRONT_OFFICE)
                .active(true)
                .build();

        userRepository.save(defaultUser);
        log.info(">>> Default user seeded. username=frontoffice | password=FrontOffice@12345");
    }
}
