package com.moodmate.backend.exception;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.LocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage(ex.getErrorCode().getMessageKey(), null, locale);

        log.warn("BusinessException: code={}, message={}, path={}", ex.getErrorCode().getCode(), message, request.getRequestURI());

        ErrorResponse response = new ErrorResponse(ex.getErrorCode().getCode(), message);
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage(ErrorCode.INVALID_CREDENTIALS.getMessageKey(), null, locale);

        log.warn("BadCredentialsException: path={}", request.getRequestURI());

        ErrorResponse response = new ErrorResponse(ErrorCode.INVALID_CREDENTIALS.getCode(), message);
        return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = messageSource.getMessage(error.getDefaultMessage(), null, error.getDefaultMessage(), locale);
            errors.put(fieldName, errorMessage);
        });

        log.warn("ValidationException: path={}, errors={}", request.getRequestURI(), errors);

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage("error.server.unexpected", null, locale);

        log.error("UnexpectedException: path={}, message={}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), message);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
    }
}