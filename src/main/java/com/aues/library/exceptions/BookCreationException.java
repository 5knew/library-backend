package com.aues.library.exceptions;

public class BookCreationException extends RuntimeException {
    public BookCreationException(String message) {
        super(message);
    }
}