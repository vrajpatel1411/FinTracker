package org.vrajpatel.userauthservice.Exception;

public class TooManyRequestException extends RuntimeException{
    public TooManyRequestException(String message){
        super(message);
    }
}
