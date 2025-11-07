package com.technicalchallenge.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.model.UserProfile;

@ExtendWith(MockitoExtension.class)
public class AuthorizationServiceTest {

    @Mock
    private ApplicationUserService applicationUserService;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    void traderCanCancelTrade() {
        ApplicationUser trader = new ApplicationUser();
        trader.setActive(true);
        UserProfile profile = new UserProfile();
        profile.setUserType("TRADER");
        trader.setUserProfile(profile);

        when(applicationUserService.getUserById(1L)).thenReturn(Optional.of(trader));

        boolean result = authorizationService.validateUserPrivileges(1L, "CANCEL_TRADE");
        assertTrue(result);
    }

    @Test
    void supportCannotCreateTrade() {
        ApplicationUser support = new ApplicationUser();
        support.setActive(true);
        UserProfile profile = new UserProfile();
        profile.setUserType("SUPPORT");
        support.setUserProfile(profile);

        when(applicationUserService.getUserById(1L)).thenReturn(Optional.of(support));

        boolean result = authorizationService.validateUserPrivileges(1L, "CREATE_TRADE");
        assertFalse(result);
    }
}
