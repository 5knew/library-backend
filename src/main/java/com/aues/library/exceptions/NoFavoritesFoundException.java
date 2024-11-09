package com.aues.library.exceptions;

public class NoFavoritesFoundException extends RuntimeException {
    // Constructor with message and cause
    public NoFavoritesFoundException(String message, Exception e) {
        super(message, e);
    }

    // Constructor with only message
    public NoFavoritesFoundException(String message) {
        super(message);
    }
}
