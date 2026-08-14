package com.arcadia.common.exception;

public class SavedGameNotFoundException extends ResourceNotFoundException {

    public SavedGameNotFoundException(String message) {
        super(message);
    }
}
