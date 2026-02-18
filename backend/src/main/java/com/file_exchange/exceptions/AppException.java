package com.file_exchange.exceptions;

import lombok.Getter;

@Getter
public abstract class AppException extends RuntimeException {
    private final int statusCode;

    protected AppException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    protected AppException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
