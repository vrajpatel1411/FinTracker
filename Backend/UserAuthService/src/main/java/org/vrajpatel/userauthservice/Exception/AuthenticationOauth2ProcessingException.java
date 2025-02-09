package org.vrajpatel.userauthservice.Exception;

import javax.naming.AuthenticationException;

public class AuthenticationOauth2ProcessingException extends AuthenticationException {

    public AuthenticationOauth2ProcessingException(String msg) {super(msg);}
    public AuthenticationOauth2ProcessingException(String msg, Throwable t) {super(msg);}
}
