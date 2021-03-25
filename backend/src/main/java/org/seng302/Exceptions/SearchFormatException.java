package org.seng302.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SearchFormatException extends ResponseStatusException {

    private static final String reason = "The search query was not formatted correctly.";
    private static final HttpStatus status = HttpStatus.BAD_REQUEST;

    public SearchFormatException() {
        super(status, reason);
    }

    public SearchFormatException(String reason) {
        super(status, reason);
    }
}
