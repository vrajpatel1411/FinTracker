package org.vrajpatel.userauthservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.UserProfileData;
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
    void findUserByEmail_userExists_returnsUserProfileData() throws UserNotFound {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserProfileData result = userService.findUserByEmail("test@example.com");

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getUserId()).isEqualTo(userId.toString());
        assertThat(result.getAuthProvider()).isEqualTo("usernamepassword");
        assertThat(result.isEmailVerified()).isTrue();
    }

    @Test
    void findUserByEmail_userMissing_throwsUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByEmail("missing@example.com"))
                .isInstanceOf(UserNotFound.class);
    }
}
