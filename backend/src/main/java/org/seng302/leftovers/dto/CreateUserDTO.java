package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A DTO representing the parameters passed to a POST /users request (to be implemented)
 */
//TODO change ^
@Getter
@ToString
public class CreateUserDTO extends UserDTO {
    @NotNull
    String password;
}
