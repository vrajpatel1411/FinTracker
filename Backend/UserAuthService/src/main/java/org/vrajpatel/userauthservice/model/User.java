package org.vrajpatel.userauthservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", nullable = false, unique = true)
    private UUID userId;

    @NotNull
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "avatar_url")
    private String photoUrl;

    @NotNull
    @Column(unique = true)
    @Email(message = "Email not valid")
    private String email;

    @Column
    private String password;

    @Column(name = "isemailverified")
    private boolean isEmailVerified;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    private String providerId;

    @NotNull
    private Instant createdAt;
}
