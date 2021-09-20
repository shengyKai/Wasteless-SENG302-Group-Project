package org.seng302.leftovers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception should be thrown when user input is provided in an invalid format. It has a 400 response code.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationResponseException extends RuntimeException {

    public ValidationResponseException(String message) {
        super(message);
    }

}
