package org.vrajpatel.userauthservice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.AuthResponse;
import org.vrajpatel.userauthservice.ResponseDTO.ValidationResponseDto;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.requestDTO.JwtDto;
import org.vrajpatel.userauthservice.requestDTO.RegisterUserDto;
import org.vrajpatel.userauthservice.requestDTO.UserDTO;
import org.vrajpatel.userauthservice.service.AuthService;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;

import java.util.Optional;

@RestController
@RequestMapping("/userauthservice/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserDTO userDTO) {
//        System.out.println(userDTO + " authcontroller.java");
        return authService.loginService(userDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserDto userDTO) {
        return authService.registerService(userDTO);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponseDto> validate(@Valid @RequestBody JwtDto jwtDto) {

        ValidationResponseDto response=new ValidationResponseDto();
        if(StringUtils.hasText(jwtDto.getJwt()) ){
            if(tokenProvider.validateToken(jwtDto.getJwt())){
               long id=tokenProvider.getUserIdFromJWT(jwtDto.getJwt());
                Optional<User> user=userRepository.findById(id);
                if(user.isPresent()){
                    response.setValid(true);
                    response.setMessage("success");
                    response.setUserEmail(user.get().getEmail());
                    return ResponseEntity.ok(response);
                }
                else{
                    response.setValid(false);
                    response.setMessage("Failed");
                    return ResponseEntity.ok(response);
                }
            };
        }
        response.setValid(false);
        response.setMessage("Failed");
        return ResponseEntity.ok(response);
    }
}
