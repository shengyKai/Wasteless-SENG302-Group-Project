package org.seng302.leftovers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an action would create a conflict e.g. trying to create an entity with a unique identifier
 * which is already in use by another entity. Has a 409 response code.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictResponseException extends RuntimeException {


    public ConflictResponseException(String message) {
        super(message);
    }

}
