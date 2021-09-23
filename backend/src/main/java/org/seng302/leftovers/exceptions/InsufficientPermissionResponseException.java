package org.seng302.leftovers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception should be thrown when a request have sufficient permission to request this operation. It has a 403
 * response code.
 */

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InsufficientPermissionResponseException extends RuntimeException {

    private static final String REASON = "You do not have permission to perform this action.";

    public InsufficientPermissionResponseException() {
        super(REASON);
    }

    public InsufficientPermissionResponseException(String reason) {
        super(reason);
    }
}
