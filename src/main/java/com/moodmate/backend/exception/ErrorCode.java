package com.moodmate.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, "error.user.notFound", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(1002, "error.email.alreadyExists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(1003, "error.credentials.invalid", HttpStatus.UNAUTHORIZED),
    MOOD_NOT_FOUND(1004, "error.mood.notFound", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS(1005, "error.unauthorized.access", HttpStatus.FORBIDDEN),
    NO_MOOD_DATA(1006, "error.advice.noMoodData", HttpStatus.BAD_REQUEST),
    NO_ADVICE_FOUND(1007, "error.advice.notFound", HttpStatus.NOT_FOUND),
    AI_SERVICE_ERROR(1008, "error.advice.aiServiceError", HttpStatus.INTERNAL_SERVER_ERROR),
    AI_RATE_LIMIT(1009, "error.advice.rateLimitExceeded", HttpStatus.TOO_MANY_REQUESTS),
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