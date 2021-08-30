package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.entities.Conversation;

/**
 * A DTO representing the parameters to a PUT /users/:id request
 */
@ToString
@Getter
public class ModifyUserDTO extends UserDTO {
    private String password;
    private String newPassword;
}
