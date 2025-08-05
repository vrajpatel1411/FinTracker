package org.vrajpatel.userauthservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="users")
@Data
@Getter @Setter @NoArgsConstructor @ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", nullable = false, unique = true)
    private UUID userId;

    @NotNull
    @Column(name = "first_name")
    private String firstName;



    @Column(name="avatar_url")
    private String photoUrl;

    @NotNull
    @Column
    @Email(message = "Email not valid")
    private String email;

    @Column
    private String password;

    @Column(name="isemailverified")
    private boolean isEmailVerified;


    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;


    private String providerId;

    @NotNull
    private Date createdAt;

}
