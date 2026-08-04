package com.arcadia.common.exception;

public class InvalidGameArchiveException extends BadRequestException {

    public InvalidGameArchiveException(String message) {
        super(message);
    }
}
