package com.sideprj.groupmeeting.advice;

import com.sideprj.groupmeeting.dto.CustomResponse;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomResponse<Object>> handleEntityNotFoundException(ResourceNotFoundException e) {
        var body = new CustomResponse<>(
                HttpStatus.NOT_FOUND.value(),
                null,
                e.getMessage() != null ? e.getMessage() : ""
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<CustomResponse<Object>> handleValidationException(HandlerMethodValidationException e) {
        var body = new CustomResponse<>(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                null,
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CustomResponse<Object>> handleBadRequestException(BadRequestException e) {
        var body = new CustomResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                null,
                e.getMessage() != null ? e.getMessage() : ""
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CustomResponse<Object>> handleUnauthorizedException(UnauthorizedException e) {
        var body = new CustomResponse<>(
                HttpStatus.UNAUTHORIZED.value(),
                null,
                e.getMessage() != null ? e.getMessage() : ""
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<Object>> otherException(Exception e) {
        log.error(e.getMessage() + "with stack trace\n" + getStackTraceAsString(e));
        var body = new CustomResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null,
                String.format("%s with stack traces: %s", e.getMessage(), getStackTraceAsString(e))
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}