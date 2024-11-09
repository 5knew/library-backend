package com.aues.library.exceptions;

public class BookCopyNotFoundException extends RuntimeException {
    // Constructor with message and cause
    public BookCopyNotFoundException(String message, Exception e) {
        super(message, e);
    }

    // Constructor with only message
    public BookCopyNotFoundException(String message) {
        super(message);
    }
}
