package com.irms.admin.exception;

public class DuplicateUserException extends IllegalArgumentException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
