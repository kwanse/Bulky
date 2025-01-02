package com.kwanse.bulky;

public class BulkyException extends RuntimeException {
    public BulkyException(String message) {
        super(message);
    }

    public BulkyException(Throwable cause) {
        super(cause);
    }
}
