package com.arcadia.common.exception;

public class SessionNotFoundException extends ResourceNotFoundException {

    public SessionNotFoundException(String message) {
        super(message);
    }
}
