package org.vrajpatel.userauthservice.Exception;

public class TooManyAttemptException extends RuntimeException{
    public TooManyAttemptException(String message){
        super(message);
    }
}
