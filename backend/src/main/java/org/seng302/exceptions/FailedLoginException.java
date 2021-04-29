package org.seng302.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a request is made to login but there is no account registered to the given email
 * or the given password does not match the authentication code for the given email.
 */
public class FailedLoginException extends ResponseStatusException {

    private static final String reason = "Email or password was incorrect.";
    private static final HttpStatus status = HttpStatus.BAD_REQUEST;

    public FailedLoginException() {
        super(status, reason);
    }

    public FailedLoginException(String reason) {
        super(status, reason);
    }
}

//TODO Needs to be deleted
