package org.liu.Common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MyExceptionHandler extends Exception{
    public int code;
    public static final String Syntax = "Syntax error";
    public static final String Runtime = "Runtime error";
    public String message;

    @Override
    public String getMessage() {
        return message;
    }
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }


}
