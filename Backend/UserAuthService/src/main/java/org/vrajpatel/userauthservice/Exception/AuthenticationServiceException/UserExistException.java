package org.vrajpatel.userauthservice.Exception.AuthenticationServiceException;

public class UserExistException extends Exception{
    public UserExistException(String message) {
        super(message);
    }
}
