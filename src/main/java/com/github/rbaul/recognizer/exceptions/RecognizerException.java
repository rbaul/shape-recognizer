package com.github.rbaul.recognizer.exceptions;

public class RecognizerException extends RuntimeException {
    public RecognizerException(String message) {
        super(message);
    }

    public RecognizerException(String message, Throwable cause) {
        super(message, cause);
    }
}
