package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * A DTO representing the parameters passed to a POST /users request
 */
@Getter
@ToString
@EqualsAndHashCode
public class CreateUserDTO extends UserDTO {
    @NotNull
    String password;
}
