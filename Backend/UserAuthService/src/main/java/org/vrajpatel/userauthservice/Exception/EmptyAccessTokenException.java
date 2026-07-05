package org.vrajpatel.userauthservice.Exception;

public class EmptyAccessTokenException extends Exception {

    public EmptyAccessTokenException(){
        super("Empty access token");
    }
}
