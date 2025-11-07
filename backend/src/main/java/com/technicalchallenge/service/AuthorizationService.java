package com.technicalchallenge.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.UserProfile;

@Service
@AllArgsConstructor
public class AuthorizationService {

    private final ApplicationUserService applicationUserService;

    public boolean authenticateUser(String userName, String password) {
        return applicationUserService.validateCredentials(userName, password);
    }

    public boolean validateUserPrivileges(Long userId, String operation) {
        ApplicationUser user = applicationUserService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isActive()) {
            return false;
        }

        UserProfile profile = user.getUserProfile();
        String role = profile.getUserType() == null ? "" : profile.getUserType().trim().toUpperCase();
        String action = operation.toUpperCase();

        return switch (role) {
            case "TRADER" -> canTrader(action);
            case "TRADER_SALES" -> canTrader(action); // This exists in the database but not in the requirements
            case "SALES" -> canSales(action);
            case "MO", "MIDDLE_OFFICE" -> canMiddleOffice(action);
            case "SUPPORT" -> canSupport(action);
            case "ADMIN", "SUPERUSER" -> true;
            default -> false;
        };
    }

    private boolean canTrader(String action) {
        return switch (action) {
            case "CREATE_TRADE", "AMEND_TRADE", "TERMINATE_TRADE", "CANCEL_TRADE", "VIEW_TRADE" -> true;
            default -> false;
        };
    }

    private boolean canSales(String action) {
        return switch (action) {
            case "CREATE_TRADE", "AMEND_TRADE", "VIEW_TRADE" -> true;
            default -> false;
        };
    }

    private boolean canMiddleOffice(String action) {
        return switch (action) {
            case "AMEND_TRADE", "VIEW_TRADE" -> true;
            default -> false;
        };
    }

    private boolean canSupport(String action) {
        return "VIEW_TRADE".equals(action);
    }
}
