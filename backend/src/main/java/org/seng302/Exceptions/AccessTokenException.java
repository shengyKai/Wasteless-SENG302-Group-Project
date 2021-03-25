package org.seng302.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This exception should be thrown when a request does not include an access token or when the access token cannot be
 * authenticated.
 */

public class AccessTokenException extends ResponseStatusException {

    private static final HttpStatus status = HttpStatus.UNAUTHORIZED;
    private static final String reason = "Invalid access token.";

    public AccessTokenException() {
        super(status, reason);
    }

    public AccessTokenException(String reason) {
        super(status, reason);
    }
}
