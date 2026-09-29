package com.military.assetmanagement.service;

import com.military.assetmanagement.audit.AuditService;
import com.military.assetmanagement.dto.user.UserRequest;
import com.military.assetmanagement.dto.user.UserResponse;
import com.military.assetmanagement.entity.Base;
import com.military.assetmanagement.entity.Role;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.exception.BadRequestException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.BaseRepository;
import com.military.assetmanagement.repository.UserRepository;
import com.military.assetmanagement.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BaseRepository baseRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse getCurrentUser(User currentUser) {
        if (currentUser == null) {
            currentUser = securityUtils.getCurrentUser();
        }
        return UserResponse.from(currentUser);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with email '" + email + "' already exists");
        }

        Role role;
        try {
            role = Role.valueOf(request.role().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.role() + ". Must be ADMIN, BASE_COMMANDER, or LOGISTICS_OFFICER");
        }

        Base base = null;
        if (request.baseId() != null) {
            base = baseRepository.findById(request.baseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Base", request.baseId()));
        } else if (role == Role.BASE_COMMANDER) {
            throw new BadRequestException("Base Commander must belong to a base (baseId is required)");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .base(base)
                .active(true)
                .build();

        User saved = userRepository.save(user);

        User actingUser = getCurrentUserSafely();
        auditService.log(
                "CREATE",
                "User",
                saved.getId(),
                saved.getBase(),
                actingUser != null ? actingUser.getId() : null,
                actingUser != null ? actingUser.getEmail() : null,
                "Created user " + saved.getEmail() + " with role " + saved.getRole()
        );

        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        String newEmail = request.email().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("User with email '" + newEmail + "' already exists");
        }

        Role role;
        try {
            role = Role.valueOf(request.role().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.role());
        }

        Base base = null;
        if (request.baseId() != null) {
            base = baseRepository.findById(request.baseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Base", request.baseId()));
        } else if (role == Role.BASE_COMMANDER) {
            throw new BadRequestException("Base Commander must belong to a base (baseId is required)");
        }

        user.setName(request.name().trim());
        user.setEmail(newEmail);
        user.setRole(role);
        user.setBase(base);

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        User updated = userRepository.save(user);

        User actingUser = getCurrentUserSafely();
        auditService.log(
                "UPDATE",
                "User",
                updated.getId(),
                updated.getBase(),
                actingUser != null ? actingUser.getId() : null,
                actingUser != null ? actingUser.getEmail() : null,
                "Updated user: " + updated.getEmail()
        );

        return UserResponse.from(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setActive(false);
        userRepository.save(user);

        User actingUser = getCurrentUserSafely();
        auditService.log(
                "DELETE",
                "User",
                user.getId(),
                user.getBase(),
                actingUser != null ? actingUser.getId() : null,
                actingUser != null ? actingUser.getEmail() : null,
                "Deactivated user: " + user.getEmail()
        );
    }

    private User getCurrentUserSafely() {
        try {
            return securityUtils.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}
