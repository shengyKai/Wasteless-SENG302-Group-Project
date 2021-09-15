package org.seng302.leftovers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.Location;

/**
 * A DTO representing the standard full JSON representation of a Location
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class LocationDTO {
    protected String country;
    protected String city;
    protected String region;
    protected String streetName;
    protected String streetNumber;
    protected String postcode;
    protected String district;

    /**
     * Converts a Location to its JSON form
     * @param location Location to convert
     * @param isFull Whether to include all parts of the location, including some that may be private
     */
    public LocationDTO(Location location, boolean isFull) {
        this.country = location.getCountry();
        this.region = location.getRegion();
        this.city = location.getCity();

        if (isFull) {
            this.district = location.getDistrict();
            this.streetName = location.getStreetName();
            this.streetNumber = location.getStreetNumber();
            this.postcode = location.getPostCode();
        }
    }

    /**
     * Helper JSON constructor
     */
    protected LocationDTO() {}

    /**
     * Generate a location from this JSON representation
     * @return Detached Location entity
     */
    public Location createLocation() {
        return new Location.Builder()
                .inCountry(country)
                .inCity(city)
                .inRegion(region)
                .onStreet(streetName)
                .atStreetNumber(streetNumber)
                .withPostCode(postcode)
                .atDistrict(district)
                .build();
    }
}
