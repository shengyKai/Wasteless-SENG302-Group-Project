package org.seng302.leftovers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when an action is taken which relates to an entity which does not exist in the database.
 * It has a 406 response code.
 */
@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class DoesNotExistResponseException extends RuntimeException {

    public DoesNotExistResponseException(Class<?> targetClass) {
        super(String.format("%s does not exist", targetClass.getSimpleName()));
    }

}
