package org.vrajpatel.userauthservice.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.InternalServerError;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserExistException;
import org.vrajpatel.userauthservice.Exception.AuthenticationServiceException.UserNotFound;
import org.vrajpatel.userauthservice.ResponseDTO.AuthResponse;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InternalServerError.class)
    public ResponseEntity<AuthResponse> handleInternalServerErrorException(InternalServerError e) {
        return buildResponse(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<AuthResponse> userNotFoundException(UserExistException e) {
        return buildResponse(e.getMessage(),HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserExistException.class)
    public ResponseEntity<AuthResponse> userExistException(UserExistException e) {
        return buildResponse(e.getMessage(),HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(AuthenticationOauth2ProcessingException.class)
    public ResponseEntity<AuthResponse> handlerAuthenticationOauth2ProcessingException(AuthenticationOauth2ProcessingException e) {
        return buildResponse(e.getMessage(),HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handlerIllegalArgumentException(IllegalArgumentException e) {
        return buildResponse(e.getMessage(),HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CustomAuthenticationError.class)
    public ResponseEntity<AuthResponse> handlerCustomAuthenticationError(CustomAuthenticationError e) {
        return buildResponse(e.getMessage(),HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<AuthResponse> handlerBadRequestException(BadRequestException e) {
        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<AuthResponse> buildResponse(String message, HttpStatus status) {
        AuthResponse resp = new AuthResponse();
        resp.setMessage(message);
        resp.setStatus(false);
        return new ResponseEntity<>(resp, status);
    }
}
