package com.example.hostinfo.util.exception;

public class NotLiveException extends RuntimeException{

    private String errMsg;

    public NotLiveException(String errMsg) {
        this.errMsg = errMsg;
    }

 }
