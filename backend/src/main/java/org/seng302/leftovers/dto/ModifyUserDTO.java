package org.seng302.leftovers.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A DTO representing the parameters to a PUT /users/:id request
 */
@ToString
@Getter
@EqualsAndHashCode
public class ModifyUserDTO extends UserDTO {
    private String password;
    private String newPassword;
}
