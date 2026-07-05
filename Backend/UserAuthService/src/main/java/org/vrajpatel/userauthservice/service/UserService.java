package org.vrajpatel.userauthservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.UserProfileData;
import org.vrajpatel.userauthservice.model.User;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserById(UUID id) throws UserNotFound {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFound("User not found: " + id));
    }

    /**
     * Returns a public-safe profile for the given email.
     * Throws {@link UserNotFound} if no account exists — callers do not need to
     * check a status flag; the exception propagates to {@code GlobalExceptionHandler}.
     */
    public UserProfileData findUserByEmail(String email) throws UserNotFound {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("No account found for the provided email"));
        return new UserProfileData(user);
    }
}
