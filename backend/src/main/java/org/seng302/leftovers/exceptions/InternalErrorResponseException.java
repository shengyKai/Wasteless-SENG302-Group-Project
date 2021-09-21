package org.seng302.leftovers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception should be thrown when an error occurs due to an issue within the application. It has a 500 response code.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalErrorResponseException extends RuntimeException {

    public InternalErrorResponseException(String message) {
        super(message);
    }

    public InternalErrorResponseException(String message, Exception cause) {
        super(message, cause);
    }

}
