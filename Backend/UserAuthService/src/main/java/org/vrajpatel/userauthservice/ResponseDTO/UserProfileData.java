package org.vrajpatel.userauthservice.ResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import org.vrajpatel.userauthservice.model.User;

/**
 * Public-safe projection of a {@link User} entity.
 * Password hash and internal OAuth2 provider ID are deliberately excluded.
 */
@Schema(description = "Public profile of the authenticated user — password hash and internal OAuth2 provider ID are excluded")
public class UserProfileData {

    @Schema(description = "UUID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String userId;

    @Schema(description = "User's first name", example = "Jane")
    private final String firstName;

    @Schema(description = "User's email address", example = "jane.doe@example.com")
    private final String email;

    @Schema(description = "Profile photo URL — populated for OAuth2 users, null for username/password accounts", example = "https://lh3.googleusercontent.com/a/example", nullable = true)
    private final String photoUrl;

    @Schema(description = "Authentication provider used to create this account", example = "usernamepassword",
            allowableValues = {"usernamepassword", "google", "github", "facebook"})
    private final String authProvider;

    @Schema(description = "True once the user's email address has been verified via OTP", example = "true")
    private final boolean emailVerified;

    @Schema(description = "ISO-8601 UTC timestamp of account creation", example = "2026-01-15T10:00:00Z")
    private final String createdAt;

    public UserProfileData(User user) {
        this.userId        = user.getUserId().toString();
        this.firstName     = user.getFirstName();
        this.email         = user.getEmail();
        this.photoUrl      = user.getPhotoUrl();
        this.authProvider  = user.getAuthProvider().name();
        this.emailVerified = user.isEmailVerified();
        this.createdAt     = user.getCreatedAt().toString();
    }

    public String  getUserId()       { return userId; }
    public String  getFirstName()    { return firstName; }
    public String  getEmail()        { return email; }
    public String  getPhotoUrl()     { return photoUrl; }
    public String  getAuthProvider() { return authProvider; }
    public boolean isEmailVerified() { return emailVerified; }
    public String  getCreatedAt()    { return createdAt; }
}
