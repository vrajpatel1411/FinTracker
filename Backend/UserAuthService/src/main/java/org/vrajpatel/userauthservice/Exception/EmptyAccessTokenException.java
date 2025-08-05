package org.vrajpatel.userauthservice.Exception;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;

public class EmptyAccessTokenException extends Exception {

    public EmptyAccessTokenException(){
        super("Expired JWT token");
    }
}
