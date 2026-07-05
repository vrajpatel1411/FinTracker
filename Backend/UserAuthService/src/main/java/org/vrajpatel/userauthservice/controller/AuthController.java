package org.vrajpatel.userauthservice.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import org.vrajpatel.userauthservice.Exception.EmptyAccessTokenException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UnAuthorizedException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.Exception.OTPException;
import org.vrajpatel.userauthservice.Exception.TooManyAttemptException;
import org.vrajpatel.userauthservice.Exception.TooManyRequestException;
import org.vrajpatel.userauthservice.ResponseDTO.*;
import org.vrajpatel.userauthservice.requestDTO.*;
import org.vrajpatel.userauthservice.service.AuthService;
import org.vrajpatel.userauthservice.utils.JwtUtils.TokenProvider;
import org.vrajpatel.userauthservice.utils.SetCookies;

import java.util.UUID;


@Tag(
        name = "Authentication",
        description = "Handles user registration, login, email OTP verification, token validation, token refresh, and logout. " +
                      "Tokens are delivered as HttpOnly cookies (accessToken: 5 min, refreshToken: 10 days). " +
                      "All responses follow the ApiResponse envelope: { success, message, data, timestamp }."
)
@RestController
@RequestMapping("/userauth/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private final SetCookies setCookies;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    AuthController(AuthService authService, TokenProvider tokenProvider, SetCookies setCookies) {
        this.authService   = authService;
        this.tokenProvider = tokenProvider;
        this.setCookies    = setCookies;
    }

    // ── Register ───────────────────────────────────────────────────────────────

    @Operation(
            summary = "Register a new user",
            description = "Creates a new account and immediately sends a 6-digit OTP to the provided email address. " +
                          "The account is not accessible until the OTP is verified via POST /verifyOtp. " +
                          "On success the response always contains an EmailVerificationData payload — " +
                          "the client should redirect to the OTP entry screen."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Account created — OTP sent, email verification required",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Email verification required",
                                      "data": { "email": "jane.doe@example.com", "needEmailVerification": true },
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="400", description = "Validation error (missing / invalid field)",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "password: Password must be at least 8 characters",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="409", description = "Email already registered",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Email Is Already Registered",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }""")))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<EmailVerificationData>> register(@Valid @RequestBody RegisterUserDto userDTO) throws Exception {
        LoginResponseDTO response = authService.registerService(userDTO);
        EmailVerificationData data = new EmailVerificationData(response.getEmail());
        return ResponseEntity.ok(ApiResponse.failure("Email verification required", data));
    }

    // ── Login ──────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Login with email and password",
            description = "Authenticates a registered user. " +
                          "If the email is already verified, accessToken and refreshToken cookies are set and data is null. " +
                          "If the email is not yet verified, an OTP is sent and the response contains an EmailVerificationData payload — " +
                          "the client should redirect to the OTP entry screen."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Login successful — tokens set as cookies",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(name = "Verified login", value = """
                                    {
                                      "success": true,
                                      "message": "Successfully logged in",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Login successful but email not yet verified — OTP sent",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(name = "Unverified email", value = """
                                    {
                                      "success": false,
                                      "message": "Email verification required",
                                      "data": { "email": "jane.doe@example.com", "needEmailVerification": true },
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="401", description = "Incorrect password",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Unauthorized Access, Password is wrong",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="404", description = "No account found with this email",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "User Not Yet Registered",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="429", description = "Too many login attempts",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Too many requests, please try again later",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }""")))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<EmailVerificationData>> login(@Valid @RequestBody LoginUserDto loginUserDto) {
        try {
            LoginResponseDTO response = authService.loginService(loginUserDto);
            if (response.isEmailVerified()) {
                return ResponseEntity.ok()
                        .headers(setCookies.setCookies(new HttpHeaders(), response.getAccessToken(), response.getRefreshToken()))
                        .body(ApiResponse.success("Successfully logged in"));
            } else {
                return ResponseEntity.ok(
                        ApiResponse.failure("Email verification required", new EmailVerificationData(response.getEmail())));
            }
        } catch (TooManyRequestException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.failure("Too many requests, please try again later"));
        } catch (UserNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (UnAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("An unexpected error occurred. Please try again."));
        }
    }

    // ── Verify OTP ─────────────────────────────────────────────────────────────

    @Operation(
            summary = "Verify email OTP",
            description = "Verifies the 6-digit OTP sent to the user's email address. " +
                          "On success the account is marked as verified, the user is logged in, and " +
                          "accessToken and refreshToken cookies are set. " +
                          "After 5 consecutive wrong attempts the OTP is invalidated — request a new one via POST /resendOtp."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "OTP verified — user logged in, tokens set as cookies",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Successfully logged in",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="400", description = "Invalid or expired OTP",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "Invalid", value = """
                                            {
                                              "success": false,
                                              "message": "Invalid OTP.",
                                              "data": null,
                                              "timestamp": "2026-04-10T10:15:30.123Z"
                                            }"""),
                                    @ExampleObject(name = "Expired", value = """
                                            {
                                              "success": false,
                                              "message": "OTP has expired. Please request a new one.",
                                              "data": null,
                                              "timestamp": "2026-04-10T10:15:30.123Z"
                                            }""")
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="429", description = "Too many wrong attempts — OTP invalidated",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Too many attempts, Request New OTP",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }""")))
    })
    @PostMapping("/verifyOtp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody OtpDto otp) {
        try {
            LoginResponseDTO loginResponseDTO = authService.verifyOTP(otp);
            return ResponseEntity.ok()
                    .headers(setCookies.setCookies(new HttpHeaders(), loginResponseDTO.getAccessToken(), loginResponseDTO.getRefreshToken()))
                    .body(ApiResponse.success("Successfully logged in"));
        } catch (OTPException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (TooManyAttemptException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during OTP verification", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }

    // ── Resend OTP ─────────────────────────────────────────────────────────────

    @Operation(
            summary = "Resend OTP",
            description = "Generates and sends a new 6-digit OTP to the given email address. " +
                          "The new OTP overwrites any existing OTP for that address (TTL: 120 seconds). " +
                          "Limited to 5 resend requests per 10-minute window per email address."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "OTP sent successfully",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "OTP sent successfully",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="429", description = "Resend rate limit exceeded (5 per 10 min)",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Too many resend attempts. Try again later.",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="500", description = "OTP delivery failed",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "An unexpected error occurred. Please try again.",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }""")))
    })
    @PostMapping("/resendOtp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestBody @Valid ResendOtpDto dto) {
        try {
            authService.sendOTP(dto.getEmail());
            return ResponseEntity.ok(ApiResponse.success("OTP sent successfully"));
        } catch (TooManyRequestException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error resending OTP to {}", dto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("An unexpected error occurred. Please try again."));
        }
    }

    // ── Validate (cookie-based — browser clients) ──────────────────────────────

    @Operation(
            summary = "Validate access token via cookies",
            description = "Validates the accessToken cookie. " +
                          "If it is expired, the refreshToken cookie is used to silently issue a new accessToken " +
                          "— the client receives a fresh cookie without re-authenticating. " +
                          "Intended for browser clients; the API Gateway should use POST /validate instead."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Token valid (or silently refreshed)",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Token is valid",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="401", description = "Both tokens absent or invalid",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Unauthorized access",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="404", description = "User record not found during refresh",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "User not found",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }""")))
    })
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Void>> validateUser(
            @CookieValue(value = "accessToken",  required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        try {
            if (accessToken == null) throw new EmptyAccessTokenException();
            tokenProvider.validateandExtractToken(accessToken);
            return ResponseEntity.ok(ApiResponse.success("Token is valid"));
        } catch (EmptyAccessTokenException | ExpiredJwtException ex) {
            try {
                if (refreshToken == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponse.failure("Session expired — please log in again"));
                }
                String newAccessToken = authService.getNewAccessToken(refreshToken);
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, setCookies.getAccessCookie(newAccessToken))
                        .body(ApiResponse.success("Token refreshed"));
            } catch (UserNotFound e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure(e.getMessage()));
            } catch (UnAuthorizedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.failure("Session expired — please log in again"));
            } catch (Exception e) {
                logger.error("Unexpected error during token refresh", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.failure("An unexpected error occurred. Please try again."));
            }
        } catch (Exception ex) {
            logger.error("Token validation error", ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Invalid or malformed token"));
        }
    }

    // ── Validate (body-based — API Gateway) ───────────────────────────────────

    @Operation(
            summary = "Validate JWT — API Gateway use",
            description = "Accepts an access token in the request body and returns the user's identity claims on success. " +
                          "Intended exclusively for the API Gateway, which calls this endpoint to identify the caller " +
                          "before forwarding requests to downstream services. " +
                          "Browser clients should use GET /validate (cookie-based) instead."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Token valid — user identity returned",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Token is valid",
                                      "data": {
                                        "userId":    "550e8400-e29b-41d4-a716-446655440000",
                                        "userEmail": "jane.doe@example.com"
                                      },
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="401", description = "Token invalid or expired",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Invalid access token",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }""")))
    })
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationData>> validate(@Valid @RequestBody JwtDto jwtDto) {
        try {
            logger.warn(jwtDto.toString());
            Claims claims = authService.validate(jwtDto.getJwt());
            String userId = claims.getSubject();
            String email  = claims.get("email", String.class);
            if (StringUtils.hasText(email)) {
                return ResponseEntity.ok(
                        ApiResponse.success("Token is valid", new TokenValidationData(userId, email)));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Invalid access token"));
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Invalid access token"));
        }
    }

    // ── Get New Access Token ───────────────────────────────────────────────────

    @Operation(
            summary = "Exchange refresh token for a new access token",
            description = "Issues a new access token from a valid refresh token. " +
                          "Pass the refresh token in the `jwt` field. " +
                          "Intended for non-browser clients (mobile, server-to-server). " +
                          "Browser clients should use GET /validate, which handles token refresh via cookies automatically."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "New access token issued",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Access token issued",
                                      "data": {
                                        "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
                                        "userId":      "550e8400-e29b-41d4-a716-446655440000",
                                        "userEmail":   "jane.doe@example.com"
                                      },
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="401", description = "Refresh token invalid or expired",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Not Authorized. Refresh Token Empty or not Valid",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "User Not Found",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }""")))
    })
    @PostMapping("/getNewAccessToken")
    public ResponseEntity<ApiResponse<AccessTokenData>> getNewAccessToken(@Valid @RequestBody JwtDto jwtDto)
            throws UserNotFound, UnAuthorizedException {
        String newAccessToken = authService.getNewAccessToken(jwtDto.getJwt());
        UUID   userId         = tokenProvider.getUserIdFromJWT(newAccessToken);
        String email          = tokenProvider.getEmailFromJWT(newAccessToken);
        return ResponseEntity.ok(
                ApiResponse.success("Access token issued", new AccessTokenData(newAccessToken, userId.toString(), email)));
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    @Operation(

            summary = "Logout",
            description = "Invalidates the user's refresh token in Redis — it can no longer be used to obtain new access tokens. " +
                          "Both accessToken and refreshToken cookies are cleared (Max-Age=0) in the response. " +
                          "Note: any access token already issued remains cryptographically valid until its 5-minute expiry."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "Logged out successfully — cookies cleared",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Logged out successfully",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }""")))
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, setCookies.clearAccessCookie());
        response.addHeader(HttpHeaders.SET_COOKIE, setCookies.clearRefreshCookie());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
