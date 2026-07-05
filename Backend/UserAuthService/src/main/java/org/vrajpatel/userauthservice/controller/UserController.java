package org.vrajpatel.userauthservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.ResponseDTO.ApiResponse;
import org.vrajpatel.userauthservice.ResponseDTO.UserProfileData;
import org.vrajpatel.userauthservice.service.UserService;
import org.vrajpatel.userauthservice.utils.UserPrincipal;


@Tag(
        name = "User",
        description = "Endpoints for retrieving the authenticated user's profile. " +
                      "All endpoints require a valid accessToken cookie."
)
@RestController
@RequestMapping("/userauth/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get current user profile",
            description = "Returns the profile of the currently authenticated user. " +
                          "Identity is resolved from the accessToken cookie — no request body or query parameters are needed. " +
                          "The password hash and internal OAuth2 provider ID are never included in the response.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="200", description = "User profile retrieved",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "User retrieved",
                                      "data": {
                                        "userId":        "550e8400-e29b-41d4-a716-446655440000",
                                        "firstName":     "Jane",
                                        "email":         "jane.doe@example.com",
                                        "photoUrl":      null,
                                        "authProvider":  "usernamepassword",
                                        "emailVerified": true,
                                        "createdAt":     "2026-01-15T10:00:00Z"
                                      },
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="401", description = "Missing or invalid accessToken cookie",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Authentication required",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode ="404", description = "Authenticated user record not found",
                    content = @Content(schema = @Schema(implementation = org.vrajpatel.userauthservice.ResponseDTO.ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "No account found for the provided email",
                                      "data": null,
                                      "timestamp": "2026-04-10T10:15:30.123Z"
                                    }""")))
    })
    @GetMapping("/")
    public ResponseEntity<ApiResponse<UserProfileData>> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Authentication required"));
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        try {
            UserProfileData profile = userService.findUserByEmail(principal.getEmail());
            return ResponseEntity.ok(ApiResponse.success("User retrieved", profile));
        } catch (UserNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }
}
