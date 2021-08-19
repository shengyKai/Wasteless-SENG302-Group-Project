package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


/**
 * A baseline UserDTO to build more children from
 */
@Getter
@ToString
public class UserDTO {
    @NotNull
    private String email;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private LocalDate dateOfBirth;
    @NotNull
    private LocationDTO homeAddress;

    private String middleName;
    private String nickname;
    private String bio;
    private String phoneNumber;

}
