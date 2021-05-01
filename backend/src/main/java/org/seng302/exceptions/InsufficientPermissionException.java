package org.seng302.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a request have sufficient permission to request this operation
 */

public class InsufficientPermissionException extends ResponseStatusException {

    private static final HttpStatus status = HttpStatus.FORBIDDEN;
    private static final String reason = "Invalid access token.";

    public InsufficientPermissionException() {
        super(status, reason);
    }

    public InsufficientPermissionException(String reason) {
        super(status, reason);
    }
}
