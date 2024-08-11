package com.sideprj.groupmeeting.exceptions;

public class BadRequestException extends Exception{
    public BadRequestException(String message){
        super(message);
    }

    public BadRequestException(){
        super();
    }
}
