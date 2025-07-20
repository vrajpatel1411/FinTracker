package org.vrajpatel.userauthservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.UserResponse;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.service.UserService;
import org.vrajpatel.userauthservice.utils.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/userauth/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<UserResponse> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        return userService.findUserByEmail(user.getEmail());
    }
}
