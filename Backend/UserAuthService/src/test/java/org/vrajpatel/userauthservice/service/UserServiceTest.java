package org.vrajpatel.userauthservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.UserResponse;
import org.vrajpatel.userauthservice.model.AuthProvider;
import org.vrajpatel.userauthservice.model.User;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    private User user;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(userId);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setEmailVerified(true);
        user.setAuthProvider(AuthProvider.usernamepassword);
        user.setCreatedAt(new Date().toInstant());
    }

    // ── findUserById ─────────────────────────────────────────────────────────

    @Test
    void findUserById_userExists_returnsUser() throws UserNotFound {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.findUserById(userId);

        assertThat(result).isEqualTo(user);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findUserById_userMissing_throwsUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(userId))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining(userId.toString());
    }

    // ── findUserByEmail ──────────────────────────────────────────────────────

    @Test
    void findUserByEmail_userExists_returnsUserResponseWithStatusTrue() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserResponse response = userService.findUserByEmail("test@example.com");

        assertThat(response.getStatus()).isTrue();
        assertThat(response.getUser()).isEqualTo(user);
        assertThat(response.getMessage()).isEqualTo("Successfully found user");
    }

    @Test
    void findUserByEmail_userMissing_returnsUserResponseWithStatusFalse() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UserResponse response = userService.findUserByEmail("missing@example.com");

        assertThat(response.getStatus()).isFalse();
        assertThat(response.getUser()).isNull();
        assertThat(response.getMessage()).isEqualTo("User not found");
    }
}
