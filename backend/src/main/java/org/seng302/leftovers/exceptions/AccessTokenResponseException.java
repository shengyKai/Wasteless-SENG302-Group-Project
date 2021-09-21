package org.seng302.leftovers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception should be thrown when a request does not include an access token or when the access token cannot be
 * authenticated. Has a 401 response code.
 */

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccessTokenResponseException extends RuntimeException {

    private static final String REASON = "Invalid access token.";

    public AccessTokenResponseException() {
        super(REASON);
    }

    public AccessTokenResponseException(String reason) {
        super(reason);
    }
}