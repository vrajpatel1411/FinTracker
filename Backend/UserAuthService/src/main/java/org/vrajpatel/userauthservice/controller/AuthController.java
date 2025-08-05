package org.vrajpatel.userauthservice.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UnAuthorizedException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserExistException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;

import org.vrajpatel.userauthservice.Exception.BadRequestException;
import org.vrajpatel.userauthservice.Exception.EmptyAccessTokenException;
import org.vrajpatel.userauthservice.Exception.OTPException;
import org.vrajpatel.userauthservice.ResponseDTO.AccessTokenResponse;
import org.vrajpatel.userauthservice.ResponseDTO.AuthResponse;
import org.vrajpatel.userauthservice.ResponseDTO.LoginResponseDTO;
import org.vrajpatel.userauthservice.ResponseDTO.ValidationResponseDto;

import org.vrajpatel.userauthservice.model.User;
import org.vrajpatel.userauthservice.requestDTO.JwtDto;
import org.vrajpatel.userauthservice.requestDTO.OtpDto;
import org.vrajpatel.userauthservice.requestDTO.RegisterUserDto;
import org.vrajpatel.userauthservice.requestDTO.UserDTO;
import org.vrajpatel.userauthservice.service.AuthService;
import org.vrajpatel.userauthservice.service.UserService;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.SetCookies;

import java.util.UUID;


@RestController
@RequestMapping("/userauth/api/auth")
public class AuthController {


    private final AuthService authService;
    private final UserService userService;
    private final TokenProvider tokenProvider;

    private final Logger logger= LoggerFactory.getLogger(AuthController.class);


    AuthController(AuthService authService, UserService userService, TokenProvider tokenProvider) {
        this.authService = authService;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }


    @Operation(
            summary = "User Login",
            description = "Allows a registered user to log in using email and password. Returns JWT access and refresh tokens as cookies if successful."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Login",
                                    value = "{ \"status\": true, \"message\": \"Successfully logged in\" }"
                            ))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Wrong password",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Wrong Password",
                                    value = "{ \"status\": false, \"message\": \"Unauthorized, Wrong Password\" }"
                            ))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "User Not Found",
                                    value = "{ \"status\": false, \"message\": \"User not found\" }"
                            ))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Unexpected server error",
                                    value = "{ \"status\": false, \"message\": \"Something went wrong. Please try again later.\" }"
                            )))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserDTO userDTO) {
        AuthResponse authResponse=new AuthResponse();

        try{
            LoginResponseDTO response=authService.loginService(userDTO);
            if(response.isEmailVerified()){
                authResponse.setStatus(true);
                authResponse.setMessage("Successfully logged in");
                return ResponseEntity.ok().headers(SetCookies.setCookies(new HttpHeaders(),response.getAccessToken(),response.getRefreshToken())).body(authResponse);
            }
            else{
                authResponse.setStatus(false);
                authResponse.setMessage("Email needs to be verified");
                authResponse.setNeedEmailVerification(true);
                authResponse.setEmail(response.getEmail());
                return ResponseEntity.ok().body(authResponse);
            }
        }
        catch (UserNotFound ex){
            authResponse.setStatus(false);
            authResponse.setMessage("User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(authResponse);
        }catch (UnAuthorizedException ex){
            authResponse.setStatus(false);
            authResponse.setMessage("Unauthorized, Wrong Password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
        }catch (Exception e){
            authResponse.setStatus(false);
            authResponse.setMessage("Internal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(authResponse);
        }
//        try {
//            LoginResponseDTO response=authService.loginService(userDTO);
//            authResponse.setStatus(true);
//            authResponse.setMessage("Successfully logged in");
//            return ResponseEntity.ok().headers(SetCookies.setCookies(new HttpHeaders(),response.getAccessToken(),response.getRefreshToken())).body(authResponse);
//
//        }catch (UserNotFound ex){
//            authResponse.setStatus(false);
//            authResponse.setMessage("User not found");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(authResponse);
//        }catch (UnAuthorizedException ex){
//            authResponse.setStatus(false);
//            authResponse.setMessage("Unauthorized, Wrong Password");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
//        }catch (Exception e){
//            authResponse.setStatus(false);
//            authResponse.setMessage("Internal Server Error");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(authResponse);
//        }
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserDto userDTO) {
        AuthResponse authResponse=new AuthResponse();
        try{
            LoginResponseDTO response=authService.registerService(userDTO);
            if(response.isEmailVerified()){
                authResponse.setStatus(true);
                authResponse.setMessage("Successfully Registered");
                return ResponseEntity.ok().headers(SetCookies.setCookies(new HttpHeaders(),response.getAccessToken(),response.getRefreshToken())).body(authResponse);
            }
            else{
                authResponse.setStatus(false);
                authResponse.setNeedEmailVerification(true);
                authResponse.setMessage("Email needs to be verified");
                authResponse.setEmail(response.getEmail());
                return ResponseEntity.ok().body(authResponse);
            }
        }
        catch (UserExistException ex){
            authResponse.setStatus(false);
            authResponse.setMessage("User Already Exist");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse);
        }
        catch (Exception ex){
            authResponse.setStatus(false);
            authResponse.setMessage("Internal Server");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(authResponse);
        }
    }


    @Operation(
            summary = "Validate Access Token via Cookies",
            description = "Validates the access token present in the cookies. If expired, uses the refresh token to issue a new access token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid or refreshed",
                    content = @Content(schema = @Schema(implementation = ValidationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access",
                    content = @Content(schema = @Schema(implementation = ValidationResponseDto.class),
                            examples = @ExampleObject(value = "{ \"valid\": false, \"message\": \"Unauthorized\" }"))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ValidationResponseDto.class),
                            examples = @ExampleObject(value = "{ \"valid\": false, \"message\": \"User not found\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ValidationResponseDto.class)))
    })
    @GetMapping("/validate")
    public ResponseEntity<ValidationResponseDto> validateUser(@CookieValue("accessToken") @Nullable  String accessToken, @CookieValue("refreshToken") String refreshToken) throws ExpiredJwtException {
        // Update according to the RefreshToken and AccessToken
        ValidationResponseDto validationResponseDto=new ValidationResponseDto();
        try {
            if(accessToken==null){
                throw new EmptyAccessTokenException();
            }
            Boolean isValid=authService.validate(accessToken);
            if(isValid){
                validationResponseDto.setValid(true);
                validationResponseDto.setMessage("Successfully logged in");
                return ResponseEntity.ok().body(validationResponseDto);
            }
            else{
                validationResponseDto.setValid(false);
                validationResponseDto.setMessage("Invalid access token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponseDto);
            }
        }catch (EmptyAccessTokenException | ExpiredJwtException ex){
            try{
                String newAccessToken=authService.getNewAccessToken(refreshToken);
                validationResponseDto.setValid(true);
                validationResponseDto.setMessage("Successfully logged in");
                String cookie=SetCookies.getAccessCookie(newAccessToken);
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie).body(validationResponseDto);

            }catch (UserNotFound e){
                validationResponseDto.setValid(false);
                validationResponseDto.setMessage("User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validationResponseDto);
            }catch (UnAuthorizedException e){
                validationResponseDto.setValid(false);
                validationResponseDto.setMessage(e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponseDto);

            }catch (Exception e){
                validationResponseDto.setValid(false);
                validationResponseDto.setMessage("Internal Server");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(validationResponseDto);
            }
        }catch (UnAuthorizedException ex){
            validationResponseDto.setValid(false);
            validationResponseDto.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponseDto);
        }
    }

    @Operation(
            summary = "Validate JWT Access Token",
            description = "Accepts a JWT and validates it. Returns user information if the token is valid."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid",
                    content = @Content(schema = @Schema(implementation = ValidationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = ValidationResponseDto.class),
                            examples = @ExampleObject(value = "{ \"valid\": false, \"message\": \"Invalid access token\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ValidationResponseDto.class)))
    })
    @PostMapping("/validate")
    public ResponseEntity<ValidationResponseDto> validate(@Valid @RequestBody JwtDto jwtDto) {
        ValidationResponseDto validationResponseDto=new ValidationResponseDto();
        try {
            Boolean isValid=authService.validate(jwtDto.getJwt());
            logger.info("TOken valid or not " + isValid);
            if (isValid) {

                UUID id = tokenProvider.getUserIdFromJWT(jwtDto.getJwt());
                User user=userService.findUserById(id);
                validationResponseDto.setValid(true);
                validationResponseDto.setMessage("Token is valid");
                validationResponseDto.setUserEmail(user.getEmail());
                validationResponseDto.setUserId(user.getUserId().toString());

                return ResponseEntity.ok().body(validationResponseDto);
            } else {
                validationResponseDto.setValid(false);
                validationResponseDto.setMessage("Invalid access token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponseDto);
            }
        }catch (Exception e){
            validationResponseDto.setValid(false);
            validationResponseDto.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponseDto);
        }
    }


    @Operation(
            summary = "Generate New Access Token from Refresh Token",
            description = "Uses a valid refresh token to generate a new access token. Returns user details and the new token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New access token generated",
                    content = @Content(schema = @Schema(implementation = AccessTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request or invalid token",
                    content = @Content(schema = @Schema(implementation = AccessTokenResponse.class),
                            examples = @ExampleObject(value = "{ \"accessToken\": null, \"userId\": null }"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = AccessTokenResponse.class)))
    })
    @PostMapping("/getNewAccessToken")
    public ResponseEntity<AccessTokenResponse> getNewAccessToken(@Valid @RequestBody JwtDto jwtDto) {
        AccessTokenResponse accessTokenResponse=new AccessTokenResponse();
        try{
            String newAccessToken=authService.getNewAccessToken(jwtDto.getJwt());
            logger.info("New access token generated");
            UUID id=tokenProvider.getUserIdFromJWT(newAccessToken);
            logger.warn(id.toString());
            User user=userService.findUserById(id);
            accessTokenResponse.setUserEmail(user.getEmail());
            accessTokenResponse.setUserId(user.getUserId().toString());
            accessTokenResponse.setAccessToken(user.getEmail());
            accessTokenResponse.setAccessToken(newAccessToken);
            return ResponseEntity.ok().body(accessTokenResponse);

        }catch (Exception e){
            throw new BadRequestException(e.getMessage());
        }
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<AuthResponse> verifyOTp(@Valid @RequestBody OtpDto otp){
        try{
            logger.warn(otp.toString());
            AuthResponse authResponse=new AuthResponse();
            LoginResponseDTO loginResponseDTO=authService.verifyOTP(otp);
            if(loginResponseDTO.isEmailVerified()){
                authResponse.setStatus(true);
                authResponse.setMessage("Successfully logged in");
                return ResponseEntity.ok().headers(SetCookies.setCookies(new HttpHeaders(),loginResponseDTO.getAccessToken(),loginResponseDTO.getRefreshToken())).body(authResponse);
            }
            else{
                authResponse.setStatus(false);
                authResponse.setMessage("Unauthorized request");
                return ResponseEntity.badRequest().body(authResponse);
            }
        }catch (OTPException e){
            AuthResponse authResponse=new AuthResponse();
            authResponse.setEmail(otp.getUserEmail());
            authResponse.setStatus(false);
            authResponse.setMessage("OTP verification failed");
            return ResponseEntity.ok().body(authResponse);

        }catch (Exception e){
            AuthResponse authResponse=new AuthResponse();
            authResponse.setStatus(false);
            authResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(authResponse);
        }
    }

    @GetMapping("/resendOtp")
    public ResponseEntity<AuthResponse> resendOTP(@RequestParam("email") String email){
        try{
            boolean res=authService.sendOTP(email);
            if(res){
                AuthResponse authResponse=new AuthResponse();
                authResponse.setEmail(email);
                authResponse.setStatus(true);
                return ResponseEntity.ok().body(authResponse);
            }
            else{
                AuthResponse authResponse=new AuthResponse();
                authResponse.setEmail(email);
                authResponse.setStatus(false);
                authResponse.setMessage("Failed to send OTP");
                return ResponseEntity.badRequest().body(authResponse);
            }
        }
        catch(Exception e){
            AuthResponse authResponse=new AuthResponse();
            authResponse.setEmail(email);
            authResponse.setStatus(false);
            authResponse.setMessage("Failed to send OTP");
            return ResponseEntity.badRequest().body(authResponse);
        }
    }
}
