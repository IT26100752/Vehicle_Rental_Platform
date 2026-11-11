package com.vehiclerental.exception;

/** Thrown when a record (user, vehicle, booking ...) does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String what, Object id) {
        return new ResourceNotFoundException(what + " with id " + id + " was not found");
    }
}
