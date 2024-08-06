package com.example.project.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    public static final String TIMESTAMP = "timestamp";
    public static final String STATUS = "status";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, Object> bodyToResponce = new LinkedHashMap<>();
        bodyToResponce.put(TIMESTAMP, LocalDateTime.now());
        bodyToResponce.put(STATUS, HttpStatus.BAD_REQUEST);
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(this::getErrorMessage)
                .toList();
        bodyToResponce.put("errors", errors);
        return new ResponseEntity<>(bodyToResponce, headers, status);
    }

    private String getErrorMessage(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField() + " " + fieldError.getDefaultMessage();
        }
        return error.getDefaultMessage();
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    protected ResponseEntity<Object> handleEntityNotFoundException(
            EntityNotFoundException ex,
            WebRequest request
    ) {
        Map<String, Object> bodyToResponce = new LinkedHashMap<>();
        bodyToResponce.put(TIMESTAMP, LocalDateTime.now());
        bodyToResponce.put(STATUS, HttpStatus.NOT_FOUND.value());
        bodyToResponce.put(ERROR, ex.getClass() + " Not Found");
        bodyToResponce.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(bodyToResponce, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {RegistrationException.class})
    protected ResponseEntity<Object> handleRegistrationException(
            RegistrationException ex,
            WebRequest request
    ) {
        Map<String, Object> bodyToResponce = new LinkedHashMap<>();
        bodyToResponce.put(TIMESTAMP, LocalDateTime.now());
        bodyToResponce.put(STATUS, HttpStatus.CONFLICT.value());
        bodyToResponce.put(ERROR, ex.getClass() + " The user already exists");
        bodyToResponce.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(bodyToResponce, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {DataNotFoundException.class})
    protected ResponseEntity<Object> handleDataNotFoundException(
            DataNotFoundException ex,
            WebRequest request
    ) {
        Map<String, Object> bodyToResponce = new LinkedHashMap<>();
        bodyToResponce.put(TIMESTAMP, LocalDateTime.now());
        bodyToResponce.put(STATUS, HttpStatus.NOT_FOUND.value());
        bodyToResponce.put(ERROR, ex.getClass() + " Not Found");
        bodyToResponce.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(bodyToResponce, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {IncorrectArgumentException.class})
    protected ResponseEntity<Object> handleIncorrectDataException(
            IncorrectArgumentException ex,
            WebRequest request
    ) {
        Map<String, Object> bodyToResponce = new LinkedHashMap<>();
        bodyToResponce.put(TIMESTAMP, LocalDateTime.now());
        bodyToResponce.put(STATUS, HttpStatus.BAD_REQUEST.value());
        bodyToResponce.put(ERROR, ex.getClass() + " Incorrect data processing");
        bodyToResponce.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(bodyToResponce, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {PaymentProcessingException.class})
    protected ResponseEntity<Object> handlePaymentProcessingException(
            PaymentProcessingException ex,
            WebRequest request
    ) {
        Map<String, Object> bodyToResponce = new LinkedHashMap<>();
        bodyToResponce.put(TIMESTAMP, LocalDateTime.now());
        bodyToResponce.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        bodyToResponce.put(ERROR, ex.getClass() + " Payment Processing Error");
        bodyToResponce.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(bodyToResponce, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(com.example.project.exception.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> bodyToResponce = new LinkedHashMap<>();
        bodyToResponce.put(TIMESTAMP, LocalDateTime.now());
        bodyToResponce.put(STATUS, HttpStatus.FORBIDDEN.value());
        bodyToResponce.put(ERROR, ex.getClass() + " Unauthorized access");
        bodyToResponce.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(bodyToResponce, HttpStatus.FORBIDDEN);
    }
}
