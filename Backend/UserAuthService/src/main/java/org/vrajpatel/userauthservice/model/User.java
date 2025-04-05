package org.vrajpatel.userauthservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
@Table(name="users")
@Data
@Getter @Setter @NoArgsConstructor @ToString
public class User {

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE,generator = "user_seq_generator")
    @SequenceGenerator(
            name="user_seq_generator",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    private Long userId;

    @NotNull
    @Column(name = "firstname")
    private String firstName;


    private String photoUrl;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "authProvider=" + authProvider +
                ", userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", providerId='" + providerId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Column
    private String lastName;

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
