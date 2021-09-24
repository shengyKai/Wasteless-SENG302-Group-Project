package org.seng302.leftovers.dto.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * A DTO representing the parameters to a PUT /users/:id request
 */
@ToString
@Getter
@EqualsAndHashCode(callSuper = false)
public class ModifyUserDTO extends BaseUserRequestDTO {
    private String password;
    private String newPassword;
    @NotNull
    private List<Long> imageIds;
}
