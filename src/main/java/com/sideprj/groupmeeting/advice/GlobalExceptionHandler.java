package com.sideprj.groupmeeting.advice;

import com.sideprj.groupmeeting.dto.CustomResponse;
import com.sideprj.groupmeeting.exceptions.UnauthorizedException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CustomResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        CustomResponse body = new CustomResponse(
                40400,
                null,
                e.getMessage() != null ? e.getMessage() : ""
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<CustomResponse> handleValidationException(HandlerMethodValidationException e) {
        var body = new CustomResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                null,
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CustomResponse> handleBadRequestException(BadRequestException e) {
        CustomResponse body = new CustomResponse(
                HttpStatus.BAD_REQUEST.value(),
                null,
                e.getMessage() != null ? e.getMessage() : ""
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CustomResponse> handleUnauthorizedException(UnauthorizedException e) {
        CustomResponse body = new CustomResponse(
                HttpStatus.UNAUTHORIZED.value(),
                null,
                e.getMessage() != null ? e.getMessage() : ""
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse> otherException(Exception e) {
        CustomResponse body = new CustomResponse(
                HttpStatus.BAD_REQUEST.value(),
                null,
                getStackTraceAsString(e)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
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