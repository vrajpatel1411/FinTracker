package org.vrajpatel.userauthservice.Exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.vrajpatel.userauthservice.ResponseDTO.AuthResponse;

import java.security.SignatureException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationOauth2ProcessingException.class)
    public ResponseEntity<AuthResponse> handlerAuthenticationOauth2ProcessingException(AuthenticationOauth2ProcessingException e) {
        AuthResponse resp = new AuthResponse();
        resp.setMessage(e.getMessage());
        resp.setStatus(false);

        return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handlerIllegalArgumentException(IllegalArgumentException e) {
        AuthResponse resp = new AuthResponse();
        resp.setMessage(e.getMessage());
        resp.setStatus(false);

        return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<AuthResponse> handlerBadRequestException(BadRequestException e) {
        AuthResponse resp = new AuthResponse();
        resp.setMessage(e.getMessage());
        resp.setStatus(false);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<AuthResponse> handleSignatureException(SignatureException ex) {
        return buildResponse("Invalid JWT Signature");
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<AuthResponse> handleMalformedJwtException(MalformedJwtException ex) {
        return buildResponse("Invalid JWT Token");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<AuthResponse> handleExpiredJwtException(ExpiredJwtException ex) {
        return buildResponse("Expired JWT Token");
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<AuthResponse> handleUnsupportedJwtException(UnsupportedJwtException ex) {
        return buildResponse("Unsupported JWT Token");
    }

    private ResponseEntity<AuthResponse> buildResponse(String message) {
        AuthResponse resp = new AuthResponse();
        resp.setMessage(message);
        resp.setStatus(false);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }
}
