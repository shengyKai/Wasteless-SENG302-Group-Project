package org.seng302.leftovers.dto.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * A DTO representing the parameters passed to a POST /users request
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class CreateUserDTO extends BaseUserRequestDTO {
    @NotNull
    String password;
}
