package com.aues.library.exceptions;

public class BookCopiesNotFoundException extends RuntimeException {
    public BookCopiesNotFoundException(String message) {
        super(message);
    }
}

