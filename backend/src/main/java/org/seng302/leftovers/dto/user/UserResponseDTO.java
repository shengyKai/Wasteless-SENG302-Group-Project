package org.seng302.leftovers.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.dto.LocationDTO;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A DTO representing a user being sent to a client
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class UserResponseDTO {
    // Public fields
    protected Long id;
    protected String firstName;
    protected String lastName;
    protected String email;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    protected Instant created;
    protected String middleName;
    protected String nickname;
    protected String bio;
    protected LocationDTO homeAddress;

    protected List<JSONObject> businessesAdministered;

    // Private fields
    protected LocalDate dateOfBirth;
    protected String phoneNumber;
    protected UserRole role;

    /**
     * Helper JSON constructor
     */
    protected UserResponseDTO() {}

    /**
     * Converts a user to its short JSON form
     * This short form does not include private information or businesses
     * @param user User to convert
     */
    public UserResponseDTO(User user) {
        this(user, false, false);
    }

    /**
     * Converts a user to its JSON form
     * @param user User to convert
     * @param includeBusinesses Whether to include information about the user's businesses
     * @param includePrivateInfo Whether to include information that is private to the user
     */
    public UserResponseDTO(User user, boolean includeBusinesses, boolean includePrivateInfo) {
        this.id = user.getUserID();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.created = user.getCreated();
        this.middleName = user.getMiddleName();
        this.nickname = user.getNickname();
        this.bio = user.getBio();
        this.homeAddress = new LocationDTO(user.getAddress(), includePrivateInfo);

        if (includePrivateInfo) {
            this.dateOfBirth = user.getDob();
            this.phoneNumber = user.getPhNum();
            this.role = user.getRole();
        }

        if (includeBusinesses) {
            this.businessesAdministered = user.getBusinessesAdministeredAndOwned()
                    .stream()
                    .sorted(Comparator.comparing(Business::getId))
                    .map(Business::constructJson)
                    .collect(Collectors.toList());
        }
    }
}
