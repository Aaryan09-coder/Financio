package com.finpro.FinancePro.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    //For testCode of UserService
    private static Long testUserId = null;

    public static Long getCurrentUserId() {

        // If a test user ID is set, return it
        if (testUserId != null) {
            return testUserId;
        }

        // Normal production logic
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        return null;
    }

    public static boolean isCurrentUserOrAdmin(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    //For TestCode

    // Method to set a test user ID (use in tests only)
    public static void setTestUserId(Long userId) {
        testUserId = userId;
    }

    // Method to clear test user ID
    public static void clearTestUserId() {
        testUserId = null;
    }
}