package org.vrajpatel.userauthservice.controller;

import io.jsonwebtoken.Claims;
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

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import org.vrajpatel.userauthservice.Exception.*;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UnAuthorizedException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;

import org.vrajpatel.userauthservice.ResponseDTO.AccessTokenResponse;
import org.vrajpatel.userauthservice.ResponseDTO.AuthResponse;
import org.vrajpatel.userauthservice.ResponseDTO.LoginResponseDTO;
import org.vrajpatel.userauthservice.ResponseDTO.ValidationResponseDto;

import org.vrajpatel.userauthservice.requestDTO.*;
import org.vrajpatel.userauthservice.service.AuthService;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.SetCookies;

import java.util.UUID;


@RestController
@RequestMapping("/userauth/api/auth")
public class AuthController {


    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private final SetCookies setCookies;

    private final Logger logger= LoggerFactory.getLogger(AuthController.class);


    AuthController(AuthService authService, TokenProvider tokenProvider, SetCookies setCookies) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
        this.setCookies = setCookies;
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginUserDto loginUserDto) {
        AuthResponse authResponse=new AuthResponse();
        try{
            LoginResponseDTO response=authService.loginService(loginUserDto);
            if(response.isEmailVerified()){
                authResponse.setStatus(true);
                authResponse.setMessage("Successfully logged in");
                return ResponseEntity.ok().headers(setCookies.setCookies(new HttpHeaders(),response.getAccessToken(),response.getRefreshToken())).body(authResponse);
            }
            else{
                authResponse.setStatus(false);
                authResponse.setMessage("Email needs to be verified");
                authResponse.setNeedEmailVerification(true);
                authResponse.setEmail(response.getEmail());
                return ResponseEntity.ok().body(authResponse);
            }
        }
        catch(TooManyRequestException e){
            authResponse.setStatus(false);
            authResponse.setMessage("Too many requests");
            authResponse.setNeedEmailVerification(true);
            authResponse.setEmail(loginUserDto.getEmail());
            return new ResponseEntity<>(authResponse,HttpStatus.TOO_MANY_REQUESTS);
        }
        catch (UserNotFound ex){
            logger.warn("User Not Found ");
            authResponse.setStatus(false);
            authResponse.setMessage("User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(authResponse);
        }catch (UnAuthorizedException ex){
            logger.warn("UnAuthorized ");
            authResponse.setStatus(false);
            authResponse.setMessage("Unauthorized, Wrong Password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
        }catch (Exception e){
            logger.warn("Internal Server Error");
            authResponse.setStatus(false);
            authResponse.setMessage("Internal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(authResponse);
        }
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserDto userDTO) throws Exception {
        AuthResponse authResponse=new AuthResponse();
        LoginResponseDTO response=authService.registerService(userDTO);
        logger.info("Service call finished");
        if(response.isEmailVerified()){
            logger.info("Registration is successful");
            authResponse.setStatus(true);
            authResponse.setMessage("Successfully Registered");
            return ResponseEntity.ok().headers(setCookies.setCookies(new HttpHeaders(),response.getAccessToken(),response.getRefreshToken())).body(authResponse);
        }
        else{
            logger.info("Registration is successful but need email verification");
            authResponse.setStatus(false);
            authResponse.setNeedEmailVerification(true);
            authResponse.setMessage("Email needs to be verified");
            authResponse.setEmail(response.getEmail());
            return ResponseEntity.ok().body(authResponse);
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
    public ResponseEntity<ValidationResponseDto> validateUser(@CookieValue(value = "accessToken", required = false) String accessToken, @CookieValue(value = "refreshToken", required = false)  String refreshToken) throws ExpiredJwtException {
        ValidationResponseDto validationResponseDto=new ValidationResponseDto();
        try {
            if(accessToken==null){
                throw new EmptyAccessTokenException();
            }
            tokenProvider.validateandExtractToken(accessToken);
            validationResponseDto.setValid(true);
            validationResponseDto.setMessage("Successfully logged in");
            return ResponseEntity.ok().body(validationResponseDto);
        }catch (EmptyAccessTokenException | ExpiredJwtException ex){
            try{
                if(refreshToken==null){
                    validationResponseDto.setValid(false);
                    validationResponseDto.setMessage("Invalid refresh token");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponseDto);
                }
                String newAccessToken=authService.getNewAccessToken(refreshToken);
                validationResponseDto.setValid(true);
                validationResponseDto.setMessage("Successfully logged in");
                String cookie=setCookies.getAccessCookie(newAccessToken);
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie).body(validationResponseDto);

            }catch (UserNotFound e){
                validationResponseDto.setValid(false);
                validationResponseDto.setMessage("User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(validationResponseDto);
            }catch (UnAuthorizedException e){
                validationResponseDto.setValid(false);
                validationResponseDto.setMessage("Unauthorized access");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponseDto);

            }catch (Exception e){
                validationResponseDto.setValid(false);
                validationResponseDto.setMessage("Internal Server");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(validationResponseDto);
            }
        }catch (Exception ex){
            validationResponseDto.setValid(false);
            logger.error("Internal Server Error ",ex);
            validationResponseDto.setMessage("Internal Server Error");
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
            Claims claims =authService.validate(jwtDto.getJwt());
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            if(StringUtils.hasText(email)) {
                validationResponseDto.setValid(true);
                validationResponseDto.setMessage("Token is valid");
                validationResponseDto.setUserEmail(email);
                validationResponseDto.setUserId(userId);
                return ResponseEntity.ok().body(validationResponseDto);
            } else {
                logger.warn("Error Validating JWT Token");
                validationResponseDto.setValid(false);
                validationResponseDto.setMessage("Invalid access token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResponseDto);
            }
        }catch (Exception e){
            logger.warn("JWT Token {}", e.getMessage());
            validationResponseDto.setValid(false);
            validationResponseDto.setMessage("Unable to validate JWT Token, Try again!");
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
    public ResponseEntity<AccessTokenResponse> getNewAccessToken(@Valid @RequestBody JwtDto jwtDto) throws UserNotFound, UnAuthorizedException {
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        String newAccessToken = authService.getNewAccessToken(jwtDto.getJwt());
        UUID id = tokenProvider.getUserIdFromJWT(newAccessToken);
        String email=tokenProvider.getEmailFromJWT(newAccessToken);
        accessTokenResponse.setUserEmail(email);
        accessTokenResponse.setUserId(id.toString());
        accessTokenResponse.setAccessToken(newAccessToken);
        return ResponseEntity.ok().body(accessTokenResponse);
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpDto otp){
        AuthResponse authResponse=new AuthResponse();
        try{
            LoginResponseDTO loginResponseDTO=authService.verifyOTP(otp);
            if(loginResponseDTO.isEmailVerified()){
                authResponse.setStatus(true);
                authResponse.setMessage("Successfully logged in");
                return ResponseEntity.ok().headers(setCookies.setCookies(new HttpHeaders(),loginResponseDTO.getAccessToken(),loginResponseDTO.getRefreshToken())).body(authResponse);
            }
            else{
                authResponse.setStatus(false);
                authResponse.setMessage("Unauthorized request");
                return ResponseEntity.badRequest().body(authResponse);
            }
        }catch (OTPException e){
            authResponse.setEmail(otp.getUserEmail());
            authResponse.setStatus(false);
            authResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(authResponse,HttpStatus.BAD_REQUEST);
        }catch (TooManyAttemptException ex){
            authResponse.setEmail(otp.getUserEmail());
            authResponse.setStatus(false);
            authResponse.setMessage(ex.getMessage());
            return new ResponseEntity<>(authResponse,HttpStatus.TOO_MANY_REQUESTS);
        }
        catch (Exception e){
            authResponse.setStatus(false);
            authResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(authResponse,HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, setCookies.clearAccessCookie());
        response.addHeader(HttpHeaders.SET_COOKIE, setCookies.clearRefreshCookie());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setStatus(true);
        authResponse.setMessage("Logged out successfully");
        return ResponseEntity.ok().body(authResponse);
    }

    @PostMapping("/resendOtp")
    public ResponseEntity<AuthResponse> resendOtp(@RequestBody @Valid ResendOtpDto dto){
        String email = dto.getEmail();
        try{
            boolean res=authService.sendOTP(email);
            AuthResponse authResponse=new AuthResponse();
            authResponse.setEmail(email);
            if(res){
                authResponse.setStatus(true);
                authResponse.setMessage("Resend OTP successfully");
                return ResponseEntity.ok().body(authResponse);
            }
            else{
                authResponse.setStatus(false);
                authResponse.setMessage("Failed to send OTP");
                return new ResponseEntity<>(authResponse,HttpStatus.BAD_REQUEST);
            }
        }
        catch (TooManyRequestException ex){
            AuthResponse authResponse=new AuthResponse();
            authResponse.setEmail(email);
            authResponse.setStatus(false);
            authResponse.setMessage(ex.getMessage());
            return new ResponseEntity<>(authResponse,HttpStatus.TOO_MANY_REQUESTS);
        }
        catch(Exception e){
            AuthResponse authResponse=new AuthResponse();
            authResponse.setEmail(email);
            authResponse.setStatus(false);
            authResponse.setMessage("Failed to send OTP");
            return new ResponseEntity<>(authResponse,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
