package com.serverless.exception;

public class IncomingRequestParsingException extends RuntimeException{

    public IncomingRequestParsingException(String message){
        super(message);
    }
}
