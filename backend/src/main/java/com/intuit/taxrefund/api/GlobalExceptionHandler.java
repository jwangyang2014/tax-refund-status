package com.intuit.taxrefund.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(IllegalArgumentException ex, HttpServletRequest req) {
        return new ApiError(
            Instant.now(),
            400,
            "Bad Request",
            ex.getMessage(),
            req.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        return new ApiError(
            Instant.now(),
            400,
            "Bad Request",
            ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(e -> e.getDefaultMessage())
                .orElse("Validation error"),
            req.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError serverError(Exception ex, HttpServletRequest req) {
        return new ApiError(
            Instant.now(),
            500,
            "Internal Server Error",
            ex.getMessage(),
            req.getRequestURI()
        );
    }
}