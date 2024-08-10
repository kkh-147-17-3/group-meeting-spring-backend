package com.sideprj.groupmeeting.exceptions;

public class ResourceNotFoundException extends Exception{

    public ResourceNotFoundException(){
        super();
    }

    public ResourceNotFoundException(String message){
        super(message);
    }
}
