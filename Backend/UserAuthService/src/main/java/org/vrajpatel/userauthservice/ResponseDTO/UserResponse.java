package org.vrajpatel.userauthservice.ResponseDTO;

import org.vrajpatel.userauthservice.model.User;

public class UserResponse {

    private User user;

    private String message;

    private Boolean status;


    public UserResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
