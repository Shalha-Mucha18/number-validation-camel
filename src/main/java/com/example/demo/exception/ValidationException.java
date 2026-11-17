package com.example.demo.exception;

/**
 * Thrown by validators when the request is not acceptable.
 * Caught by the global onException in camel-context.xml and mapped to EX002 (slide 16).
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}