package com.aues.library.exceptions;

public class BookUpdateException extends RuntimeException {
    public BookUpdateException(String message, Exception e) {
        super(message);
    }
}