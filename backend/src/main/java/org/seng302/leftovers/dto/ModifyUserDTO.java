package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * A DTO representing the parameters to a PUT /users/:id request
 */
@ToString
@Getter
public class ModifyUserDTO extends UserDTO {
    private String password;
    private String newPassword;
}
