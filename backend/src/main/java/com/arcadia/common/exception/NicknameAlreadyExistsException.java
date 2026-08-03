package com.arcadia.common.exception;

public class NicknameAlreadyExistsException extends ConflictException {

    public NicknameAlreadyExistsException(String message) {
        super(message);
    }
}
