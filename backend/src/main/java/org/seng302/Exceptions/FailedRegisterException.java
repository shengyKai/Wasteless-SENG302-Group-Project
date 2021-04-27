package org.seng302.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a user attempts to register but one or more or the parameter they have entered
 * does not pass the validation checks for the user class.
 */
public class FailedRegisterException extends ResponseStatusException {

    private static final String reason = "One or more fields was invalid.";
    private static final HttpStatus status = HttpStatus.BAD_REQUEST;

    public FailedRegisterException() {
        super(status, reason);
    }

    public FailedRegisterException(String reason) {
        super(status, reason);
    }
}

//TODO This exception is no longer being used. Discuss with the group whether this should be deleted