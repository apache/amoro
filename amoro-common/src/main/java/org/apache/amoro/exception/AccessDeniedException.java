package org.apache.amoro.exception;

public class AccessDeniedException extends AmoroRuntimeException {
    public AccessDeniedException() {}

    public AccessDeniedException(String message) {
        super(message);
    }
}
