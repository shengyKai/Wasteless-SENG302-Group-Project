package org.seng302.leftovers.dto.business;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.dto.LocationDTO;
import org.seng302.leftovers.entities.Business;

import javax.validation.constraints.NotNull;

/**
 * A DTO representing the parameters passed to a POST /businesses request
 */
@Getter
@ToString
@EqualsAndHashCode
public class CreateBusinessDTO {
    @NotNull
    private Long primaryAdministratorId;
    @NotNull
    private String name;
    @NotNull
    private String description;
    @NotNull
    private LocationDTO address;
    @NotNull
    private BusinessType businessType; // In future it'd be nice if this was an enum

    /**
     * Constructs a create business request from a business
     * This is only expected to be used by the BusinessResponseDTO constructor
     * @param business Business to make request from
     */
    protected CreateBusinessDTO(Business business) {
        this.primaryAdministratorId = business.getPrimaryOwner().getUserID();
        this.name = business.getName();
        this.description = business.getDescription();
        this.address = new LocationDTO(business.getAddress(), true);
        this.businessType = business.getBusinessType();
    }

    /**
     * Helper JSON constructor
     */
    protected CreateBusinessDTO() {}
}
