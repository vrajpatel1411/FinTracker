package org.vrajpatel.userauthservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.vrajpatel.userauthservice.ResponseDTO.UserResponse;
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
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

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
    void getUser_authenticated_returns200WithUser() throws Exception {
        setAuthentication();

        UserResponse userResponse = new UserResponse();
        userResponse.setUser(user);
        userResponse.setMessage("Successfully found user");
        userResponse.setStatus(true);
        when(userService.findUserByEmail("test@example.com")).thenReturn(userResponse);

        mockMvc.perform(get("/userauth/api/user/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully found user"))
                .andExpect(jsonPath("$.status").value(true));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getUser_userNotFoundByEmail_returns404() throws Exception {
        setAuthentication();

        UserResponse notFound = new UserResponse();
        notFound.setMessage("User not found");
        notFound.setStatus(false);
        when(userService.findUserByEmail("test@example.com")).thenReturn(notFound);

        mockMvc.perform(get("/userauth/api/user/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getUser_nullAuthentication_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/userauth/api/user/"))
                .andExpect(status().isUnauthorized());
    }
}
