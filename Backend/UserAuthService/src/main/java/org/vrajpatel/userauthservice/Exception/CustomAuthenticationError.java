package org.vrajpatel.userauthservice.Exception;

import javax.naming.AuthenticationException;

public class CustomAuthenticationError extends AuthenticationException {

    public CustomAuthenticationError(String message) {
        super(message);
    }

}
