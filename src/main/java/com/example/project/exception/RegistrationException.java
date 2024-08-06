package com.example.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "The user already exists")
public class RegistrationException extends Exception {
    public RegistrationException(String message) {
        super(message);
    }
}
