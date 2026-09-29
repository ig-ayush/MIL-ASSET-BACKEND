package com.military.assetmanagement.service;

import com.military.assetmanagement.audit.AuditService;
import com.military.assetmanagement.dto.auth.LoginRequest;
import com.military.assetmanagement.dto.auth.LoginResponse;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.exception.BadRequestException;
import com.military.assetmanagement.repository.UserRepository;
import com.military.assetmanagement.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuditService auditService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadRequestException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Invalid password attempt for email: {}", request.email());
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtUtils.generateToken(user);

        Long baseId = user.getBase() != null ? user.getBase().getId() : null;
        String baseName = user.getBase() != null ? user.getBase().getName() : null;

        auditService.log(
                "LOGIN",
                "User",
                user.getId(),
                user.getBase(),
                user.getId(),
                user.getEmail(),
                "User logged in successfully"
        );

        log.info("User {} logged in successfully with role {}", user.getEmail(), user.getRole());

        return LoginResponse.of(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                baseId,
                baseName
        );
    }
}
