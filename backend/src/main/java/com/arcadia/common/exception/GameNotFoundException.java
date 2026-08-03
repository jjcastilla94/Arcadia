package com.arcadia.common.exception;

public class GameNotFoundException extends ResourceNotFoundException {

    public GameNotFoundException(String message) {
        super(message);
    }
}
