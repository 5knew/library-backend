package com.aues.library.exceptions;

public class UserUpdateException extends RuntimeException {
    // Constructor with message and cause
    public UserUpdateException(String message, Exception e) {
        super(message, e);
    }

    // Constructor with only message
    public UserUpdateException(String message) {
        super(message);
    }
}
