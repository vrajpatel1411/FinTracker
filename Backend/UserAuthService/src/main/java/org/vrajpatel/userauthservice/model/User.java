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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }



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

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;


    private String providerId;

    @NotNull
    private Date createdAt;

}
