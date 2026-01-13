package com.moodmate.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, "error.user.notFound", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(1002, "error.email.alreadyExists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(1003, "error.credentials.invalid", HttpStatus.UNAUTHORIZED),
    INTERNAL_SERVER_ERROR(9000, "error.server.internal", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String messageKey;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String messageKey, HttpStatus httpStatus) {
        this.code = code;
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }
}