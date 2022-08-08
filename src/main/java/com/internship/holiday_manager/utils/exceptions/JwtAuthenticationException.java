package com.internship.holiday_manager.utils.exceptions;

public class JwtAuthenticationException extends RuntimeException{
    public JwtAuthenticationException(final Exception ex){
        super(ex);
    }
}
