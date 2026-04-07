package org.vrajpatel.userauthservice.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.UserResponse;
import org.vrajpatel.userauthservice.model.User;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserById(UUID id) throws UserNotFound {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFound("User not found: " + id));
    }

    public UserResponse findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        UserResponse userResponse = new UserResponse();
        if(user.isPresent()) {
            userResponse.setUser(user.get());
            userResponse.setMessage("Successfully found user");
            userResponse.setStatus(true);
        }
        else{
            userResponse.setMessage("User not found");
            userResponse.setStatus(false);
        }
        return userResponse;
    }


}
