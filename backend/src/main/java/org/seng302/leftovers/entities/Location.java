package org.seng302.leftovers.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.seng302.leftovers.exceptions.ValidationResponseException;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

@Data // generate setters and getters for all fields (lombok pre-processor)
@NoArgsConstructor // generate a no-args constructor needed by JPA (lombok pre-processor)
@ToString // generate a toString method
@Entity // declare this class as a JPA entity (that can be mapped to a SQL table)
public class Location {
    private static final String NAME_REGEX = "[ \\p{L}-'.]+";
    private static final String STREET_NAME_REGEX = "[ \\p{L}-'.\\d]+";

    @Id // this field (attribute) is the table primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement the ID
    private Long id;

    @Column(name = "country", nullable = false)
    private  String country;
    @Column(name = "city", nullable = false)
    private  String city;

    @Column(name="region", nullable = false)
    private String region;

    @Column(name="street_name", nullable = false)
    private String streetName;

    @Column(name="street_number", nullable = false)
    private String streetNumber;

    @Column(name="post_code", nullable = false)
    private String postCode;

    @Column(name="district")
    private String district;

    /**
     * Converts the address string into a location object
     * @param address
     * @return
     */
    public static Location covertAddressStringToLocation(String address) {
        List<String> addressComponents = Arrays.asList(address.split(","));
        String streetNumber = addressComponents.get(0);
        String streetName = addressComponents.get(1);
        String district = addressComponents.get(2);
        String city = addressComponents.get(3);
        String country = addressComponents.get(4);
        String region = addressComponents.get(5);
        String postCode = addressComponents.get(6);
        Builder locationBuilder = new Builder().atStreetNumber(streetNumber).onStreet(streetName)
                .inCity(city).inRegion(region).inCountry(country).withPostCode(postCode).atDistrict(district);
        return locationBuilder.build();
    }

    /**
     * Checks all the parameters of the location are valid
     * @param location the location encapsulating a given address
     * @return true if all the parameters of the location are valid, false otherwise
     */
    public boolean checkValidAllLocationParameters(Location location) {
        if (!checkValidStreetNumber(location.getStreetNumber())) {
            return false;
        }
        if (!checkValidStreetName(location.getStreetName())) {
            return false;
        }
        if (!checkValidCity(location.getCity())) {
            return false;
        }
        if (!checkValidRegion(location.getRegion())) {
            return false;
        }
        if (!checkValidCountry(location.getCountry())) {
            return false;
        }
        if (!checkValidDistrict(location.getDistrict())) {
            return false;
        }
        return checkValidPostCode(location.getPostCode());
    }

    /**
     * Checks the street number is valid.
     * The current highest number street in the world is 304, however, certain addresses can have / in them such as
     * 130/2 to refer to sub-units. Therefore we wil assume that there will never be more than 9999 houses on an
     * individual street and that there will not be any more than 9999 sub-units. It will also contain less of equal to
     * one backslash. Thus, the max length will be 9 characters long.
     * @param streetNumber The street number of the location
     * @return true if the street number is valid, false otherwise
     */
    public boolean checkValidStreetNumber(String streetNumber) {
        return streetNumber != null && streetNumber.length() > 0 && streetNumber.length() <= 9 && streetNumber.matches("([0-9]+|[0-9]+\\/[0-9]+)[\\p{L}]?");
    }

    /**
     * Checks the name of the street is valid
     * Realistically no street name should be over 100 characters and the longest place name is 85 characters. Therefore,
     * the street name must be below 100 characters and have at least one character. Additionally, the street name can
     * only contain letters. Foreig addresses that use characters such as Japanese Kanji are expected to put in the
     * English version of the address.
     * @param streetName the street address of the location
     * @return true if the street name is valid, false otherwise
     */
    public boolean checkValidStreetName(String streetName) {
        return streetName != null && streetName.length() <= 100 && streetName.length() > 0 && streetName.matches(STREET_NAME_REGEX);
    }

    /**
     * Checks the name of the city is valid
     * Realistically no city name will be over 100 characters, they are also generally one or two words. Therefore, the
     * city name must be below 50 characters and have at least one character. Additionally, foreign addresses are
     * expected to be put in the English version, thus, the city must only contain letters.
     * @param city the city of the location
     * @return true if the city name is valid, false otherwise
     */
    public boolean checkValidCity(String city) {
        return city != null && city.length() < 100 && city.length() > 0 && city.matches(NAME_REGEX);
    }

    /**
     * Checks the name of the region is valid
     * Realisitcally no region name will be over 100 characters, they are also generally are one word. Therefore, the
     * region name must be below 50 characters and have at least one character. Additionally, foreign addresses are
     * expected to be put in the English version, thus, the region must only contain letters.
     * @param region the city of the location
     * @return true if the region name is valid, false otherwise
     */
    public boolean checkValidRegion(String region) {
        return region != null && region.length() < 100 && region.length() > 0 && region.matches(NAME_REGEX);
    }

    /**
     * Checks the name of the country is valid
     * Realisitcally no region name will be over 100 characters, they are also generally are one word. Therefore, the
     * country name must be below 50 characters and have at least one character. Additionally, foreign addresses are
     * expected to be put in the English version, thus, the country must only contain letters.
     * @param country the country of the location
     * @return true if the country name is valid, false otherwise
     */
    public boolean checkValidCountry(String country) {
        return country != null && country.length() < 100 && country.length() > 0 && country.matches(NAME_REGEX);
    }

    /**
     * Checks the post code number is valid
     * America has the largest post codes which include 9 digits and some countries like Canada use letters within there
     * post codes. Therefore, the post code must contain less or equal to 9 characters and above 0. Additionally, the
     * post code must be alphanumberic.
     * @param postCode the post code of the location
     * @return true if the post code number is valid, false otherwise
     */
    public boolean checkValidPostCode(String postCode) {
        return postCode != null && postCode.length() <= 16 && postCode.length() > 0 && postCode.matches("[\\p{L}0-9]+");
    }
    
    /**
     * Checks if the district is valid
     * There are some districts with long names, as such we are targetting the length of the characters to be at most 100 characters.
     * Some districts also have numbers in them so that has to be checked. District field is not mandatory, so it can be an empty
     * string.
     * @param district district of the location
     * @return true if the district is valid, false otherwise
     */
    public boolean checkValidDistrict(String district) {
        return district == null || district.isEmpty() || (district.length() <= 100 && district.matches("[ \\p{L}0-9.'-]+"));
    }

    public Long getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCountry(String country) {
        if (checkValidCountry(country)) {
            this.country = country;
        } else {
            throw new ValidationResponseException("The country must not be empty, be less then 32 characters, and only contain letters.");
        }
    }

    public void setCity(String city) {
        if (checkValidCity(city)) {
            this.city = city;
        } else {
            throw new ValidationResponseException("The city must not be empty, be less then 32 characters, and only contain letters.");
        }
    }

    public void setRegion(String region) {
        if (checkValidRegion(region)) {
            this.region = region;
        } else {
            throw new ValidationResponseException("The region must not be empty, be less then 32 characters, and only contain letters.");
        }
    }

    public void setStreetName(String streetName) {
        if (checkValidStreetName(streetName)) {
            this.streetName = streetName;
        } else {
            throw new ValidationResponseException("The street name must not be empty, be less then 100 characters, and only contain letters.");
        }
    }

    public void setStreetNumber(String streetNumber) {
        if (checkValidStreetNumber(streetNumber)) {
            this.streetNumber = streetNumber;
        } else {
            throw new ValidationResponseException("The street number must not be empty, and be less than 10 characters.");
        }
    }

    public void setPostCode(String postCode) {
        if (checkValidPostCode(postCode)) {
            this.postCode = postCode;
        } else {
            throw new ValidationResponseException("The post code must be a letter or number, be " +
                    "less than 16 characters long, and at least one character long.");
        }
    }
    
    public void setDistrict(String district) {
        if (checkValidDistrict(district)) {
            this.district = district;
        } else {
            throw new ValidationResponseException("The district must not be more than 100 characters long.");
        }
    }

    /**
     * This class uses the builder pattern to construct an instance of the Location class
     */
    public static class Builder {

        private String country;
        private String city;
        private String region;
        private String streetName;
        private String streetNumber;
        private String postCode;
        private String district;

        /**
         * Set the builder's country.
         * @param country A string representing a country.
         * @return Builder with country parameter set.
         */
        public Builder inCountry(String country) {
            this.country = country;
            return this;
        }

        /**
         * Set the builder's city.
         * @param city A string representing a city.
         * @return Builder with city parameter set.
         */
        public Builder inCity(String city) {
            this.city = city;
            return this;
        }

        /**
         * Set the builder's region.
         * @param region A string representing a region.
         * @return Builder with region parameter set.
         */
        public Builder inRegion(String region) {
            this.region = region;
            return this;
        }

        /**
         * Set the builder's street name.
         * @param streetName A string representing a street name.
         * @return Builder with street name parameter set.
         */
        public Builder onStreet(String streetName) {
            this.streetName = streetName;
            return this;
        }

        /**
         * Set the builder's street number.
         * @param streetNumber An integer representing a street number.
         * @return Builder with street number parameter set.
         */
        public Builder atStreetNumber(String streetNumber) {
            this.streetNumber = streetNumber;
            return this;
        }

        /**
         * Set the builder's post code.
         * @param postCode A string representing a post code.
         * @return Builder with post code parameter set.
         */
        public Builder withPostCode(String postCode)  {
            this.postCode = postCode;
            return this;
        }

        /**
         * Set the builder's district.
         * @param district A string representing a district.
         * @return Builder with post code parameter set.
         */
        public Builder atDistrict(String district)  {
            if (district == null || district.equals("")) {
                district = "";
            }
            this.district = district;
            return this;
        }

        /**
         * Construct an instance of the Location class.
         * @return Location with parameters of Builder.
         */
        public Location build() {
            Location location = new Location();
            location.setCountry(this.country);
            location.setCity(this.city);
            location.setRegion(this.region);
            location.setStreetName(this.streetName);
            location.setStreetNumber(this.streetNumber);
            location.setPostCode(this.postCode);
            location.setDistrict(this.district);
            return location;
        }

    }
}
