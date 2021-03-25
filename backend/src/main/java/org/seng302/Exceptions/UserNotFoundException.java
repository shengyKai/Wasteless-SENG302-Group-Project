package org.seng302.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a get request is made for a user id which is not present in the application's
 * database.
 */
public class UserNotFoundException extends ResponseStatusException {

    private static final String reason = "User not found.";
    private static final HttpStatus status = HttpStatus.NOT_ACCEPTABLE;

    public UserNotFoundException() {
        super(status, reason);
    }

    public UserNotFoundException(String reason) {
        super(status, reason);
    }
}
