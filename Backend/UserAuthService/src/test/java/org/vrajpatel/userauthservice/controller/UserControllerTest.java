package org.vrajpatel.userauthservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.Exception.GlobalExceptionHandler;
import org.vrajpatel.userauthservice.ResponseDTO.UserProfileData;
import org.vrajpatel.userauthservice.model.AuthProvider;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.service.UserService;
import org.vrajpatel.userauthservice.utils.UserPrincipal;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock UserService userService;
    @InjectMocks UserController userController;

    MockMvc mockMvc;
    private final UUID userId = UUID.randomUUID();
    private UserPrincipal principal;
    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        user = new User();
        user.setUserId(userId);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setEmailVerified(true);
        user.setAuthProvider(AuthProvider.usernamepassword);
        user.setCreatedAt(new Date().toInstant());

        principal = new UserPrincipal(userId, "test@example.com", "pw", true,
                Collections.emptyList());
    }

    private void setAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── GET /userauth/api/user/ ──────────────────────────────────────────────

    @Test
    void getUser_authenticated_returns200WithProfile() throws Exception {
        setAuthentication();
        when(userService.findUserByEmail("test@example.com"))
                .thenReturn(new UserProfileData(user));

        mockMvc.perform(get("/userauth/api/user/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User retrieved"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Test"))
                .andExpect(jsonPath("$.data.authProvider").value("usernamepassword"))
                .andExpect(jsonPath("$.data.emailVerified").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getUser_userNotFound_returns404() throws Exception {
        setAuthentication();
        when(userService.findUserByEmail("test@example.com"))
                .thenThrow(new UserNotFound("No account found for the provided email"));

        mockMvc.perform(get("/userauth/api/user/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No account found for the provided email"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getUser_nullAuthentication_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/userauth/api/user/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
