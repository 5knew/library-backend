package com.aues.library.exceptions;

public class BookDeletionException extends RuntimeException {
    public BookDeletionException(String message, Exception e) {
        super(message);
    }
}