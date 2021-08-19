package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * A DTO representing a JSON object with the shape:
 * {
 *     "value": Value of type T
 * }
 * @param <T> Type to wrap
 */
@Getter
@ToString
public class WrappedValueDTO<T> {
    @NotNull
    private T value;
}
