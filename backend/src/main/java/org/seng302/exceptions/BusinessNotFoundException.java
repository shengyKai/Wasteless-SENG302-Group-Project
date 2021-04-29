package org.seng302.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a get request is made for a user id which is not present in the application's
 * database.
 */
public class BusinessNotFoundException extends ResponseStatusException {

    private static final String reason = "Business not found.";
    private static final HttpStatus status = HttpStatus.NOT_ACCEPTABLE;

    public BusinessNotFoundException() {
        super(status, reason);
    }

    public BusinessNotFoundException(String reason) {
        super(status, reason);
    }
}
