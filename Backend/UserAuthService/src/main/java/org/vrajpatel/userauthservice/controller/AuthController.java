package org.vrajpatel.userauthservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserExistException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.Repository.UserRepository;
import org.vrajpatel.userauthservice.ResponseDTO.AuthResponse;
import org.vrajpatel.userauthservice.ResponseDTO.ValidationResponseDto;
import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.requestDTO.JwtDto;
import org.vrajpatel.userauthservice.requestDTO.RegisterUserDto;
import org.vrajpatel.userauthservice.requestDTO.UserDTO;
import org.vrajpatel.userauthservice.service.AuthService;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/userauth/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;


    @Operation(summary = "Login " , description="User can login through credential and sent back jwt token if successfull")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Register Successfull",content =  @Content(schema = @Schema(implementation = AuthResponse.class ))),
            @ApiResponse(responseCode = "400", description = "User email Already Exist",content =  @Content(schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(name="User Exist Error", value="{ \"status\": false, \"message\": \"Email is already registered\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server", content = @Content(schema = @Schema(implementation = AuthResponse.class ), examples = @ExampleObject(
                    name = "ServerError",
                    summary = "Unexpected error",
                    value = "{ \"status\": false, \"message\": \"Something went wrong. Please try again later.\" }"
            )))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserDTO userDTO) throws UserNotFound {
//        System.out.println(userDTO + " authcontroller.java");
        return authService.loginService(userDTO);
    }

    @Operation(summary = "Registering a new User" , description="Register New User based on information provided and sent back jwt token if successfull")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User Register Successfull",content =  @Content(schema = @Schema(implementation = AuthResponse.class ))),
            @ApiResponse(responseCode = "400", description = "User email Already Exist",content =  @Content(schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(name="User Exist Error", value="{ \"status\": false, \"message\": \"Email is already registered\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server", content = @Content(schema = @Schema(implementation = AuthResponse.class ), examples = @ExampleObject(
                    name = "ServerError",
                    summary = "Unexpected error",
                    value = "{ \"status\": false, \"message\": \"Something went wrong. Please try again later.\" }"
            )))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserDto userDTO)  throws UserExistException {
        return authService.registerService(userDTO);
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidationResponseDto> validateUser(@CookieValue("jwttoken") String token) {
        ValidationResponseDto response=new ValidationResponseDto();
        if(token !=null && tokenProvider.validateToken(token)) {
            response.setValid(true);
            response.setMessage("success");
            return ResponseEntity.ok(response);
        }
        else{
            response.setValid(false);
            response.setMessage("Unauthorized User or token");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponseDto> validate(@Valid @RequestBody JwtDto jwtDto) {
        ValidationResponseDto response=new ValidationResponseDto();
        if(StringUtils.hasText(jwtDto.getJwt()) ){
            if(tokenProvider.validateToken(jwtDto.getJwt())){
               UUID id=tokenProvider.getUserIdFromJWT(jwtDto.getJwt());
                Optional<User> user=userRepository.findByUserId(id);
                if(user.isPresent()){
                    response.setValid(true);
                    response.setMessage("success");
                    response.setUserEmail(user.get().getEmail());
                    response.setUserId(user.get().getUserId().toString());
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
