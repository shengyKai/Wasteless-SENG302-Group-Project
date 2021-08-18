package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.Location;

/**
 * A DTO representing the standard full JSON representation of a Location
 */
@Getter
@ToString
public class LocationDTO {
    private String country;
    private String city;
    private String region;
    private String streetName;
    private String streetNumber;
    private String postcode;
    private String district;

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