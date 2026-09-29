package com.military.assetmanagement.security;

import com.military.assetmanagement.entity.Role;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.exception.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper to get the currently authenticated user and enforce
 * base-level RBAC without trusting any client-supplied base IDs.
 */
@Component
public class SecurityUtils {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user");
        }
        return (User) auth.getPrincipal();
    }

    public boolean isAdmin() {
        return getCurrentUser().getRole() == Role.ADMIN;
    }

    public boolean isBaseCommander() {
        return getCurrentUser().getRole() == Role.BASE_COMMANDER;
    }

    public boolean isLogisticsOfficer() {
        return getCurrentUser().getRole() == Role.LOGISTICS_OFFICER;
    }

    /**
     * Resolves which base the current user may access.
     * ADMIN: uses the provided baseId (can be null to see all).
     * BASE_COMMANDER / LOGISTICS_OFFICER: always returns their own base id.
     * Throws AccessDeniedException if a non-admin tries to access a different base.
     */
    public Long resolveBaseId(Long requestedBaseId) {
        User user = getCurrentUser();
        if (user.getRole() == Role.ADMIN) {
            return requestedBaseId;
        }
        Long userBaseId = user.getBase() != null ? user.getBase().getId() : null;
        if (requestedBaseId != null && !requestedBaseId.equals(userBaseId)) {
            throw new AccessDeniedException("You are not authorized to access data for base id: " + requestedBaseId);
        }
        return userBaseId;
    }

    /**
     * Enforces that a BASE_COMMANDER can only operate on their own base.
     * Called before any write operation that includes a baseId.
     */
    public void enforceBaseAccess(Long targetBaseId) {
        User user = getCurrentUser();
        if (user.getRole() == Role.ADMIN) return;
        Long userBaseId = user.getBase() != null ? user.getBase().getId() : null;
        if (!targetBaseId.equals(userBaseId)) {
            throw new AccessDeniedException("Access denied: you do not belong to base id " + targetBaseId);
        }
    }
}
