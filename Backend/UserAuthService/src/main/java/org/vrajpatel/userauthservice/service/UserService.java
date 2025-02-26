package org.vrajpatel.userauthservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.UserResponse;
import org.vrajpatel.userauthservice.model.User;

import java.util.Optional;

@Service
public class UserService {

    public final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<UserResponse> findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()) {
            UserResponse userResponse = new UserResponse();
            userResponse.setUser(user.get());
            userResponse.setMessage("Successfully found user");
            userResponse.setStatus(true);
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        }
        else{
            UserResponse userResponse = new UserResponse();
            userResponse.setMessage("User not found");
            userResponse.setStatus(false);
            return new ResponseEntity<>(userResponse,HttpStatus.NOT_FOUND);
        }
    }
}
