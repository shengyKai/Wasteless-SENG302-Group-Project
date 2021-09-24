package org.seng302.leftovers.dto.business;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.ImageDTO;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.entities.Account;
import org.seng302.leftovers.entities.Business;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A DTO representing a business being sent to a client
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessResponseDTO extends CreateBusinessDTO {
    private Long id;
    private List<ImageDTO> images;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Instant created;

    private List<UserResponseDTO> administrators;

    /**
     * Creates a JSON representation of the business that includes the admin details
     * @param business Business to represent
     * @return JSON representation of the business with admins
     */
    public static BusinessResponseDTO withAdmins(Business business) {
        return new BusinessResponseDTO(business, true);
    }

    /**
     * Creates a JSON representation of the business without the admin details included
     * @param business Business to represent
     * @return JSON representation of the business
     */
    public static BusinessResponseDTO withoutAdmins(Business business) {
        return new BusinessResponseDTO(business, false);
    }


    /**
     * Constructor for the BusinessResponseDTO
     * The static helper methods should be used instead of calling this directly
     * @param business Business to make the DTO for
     * @param includeAdminDetails Whether to include information about the admins of the business
     */
    protected BusinessResponseDTO(Business business, boolean includeAdminDetails) {
        super(business);
        this.id = business.getId();
        this.images = business.getImages().stream().map(ImageDTO::new).collect(Collectors.toList());
        this.created = business.getCreated();

        if (includeAdminDetails) {
            this.administrators = business.getOwnerAndAdministrators().stream()
                    .sorted(Comparator.comparing(Account::getUserID))
                    .map(UserResponseDTO::new)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Helper JSON constructor
     */
    protected BusinessResponseDTO() {}
}
