package com.file_exchange.exceptions;

public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(message, 400);
    }
}
