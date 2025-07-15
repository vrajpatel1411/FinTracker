package org.vrajpatel.userauthservice.Exception.AuthenticationServiceException;

public class InternalServerError extends RuntimeException {
    public InternalServerError(String message) {
        super(message);
    }
}
