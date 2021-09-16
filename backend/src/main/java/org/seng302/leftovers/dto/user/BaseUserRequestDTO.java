package org.seng302.leftovers.dto.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.LocationDTO;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;


/**
 * A base DTO for client requests to create/update users
 */
@Getter
@ToString
@EqualsAndHashCode
public class BaseUserRequestDTO {
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
