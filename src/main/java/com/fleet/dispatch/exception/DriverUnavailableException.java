package com.fleet.dispatch.exception;

public class DriverUnavailableException extends RuntimeException {
    public DriverUnavailableException(String message) {
        super(message);
    }
}
