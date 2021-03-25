package org.seng302.Entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;

@Data // generate setters and getters for all fields (lombok pre-processor)
@NoArgsConstructor // generate a no-args constructor needed by JPA (lombok pre-processor)
@ToString // generate a toString method
@Entity // declare this class as a JPA entity (that can be mapped to a SQL table)
public class Location {

    @Id // this field (attribute) is the table primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement the ID
    private Long id;

    @Column(name = "country")
    private  String country;
    @Column(name = "city")
    private  String city;

    @Column(name="region")
    private String region;

    @Column(name="street_name")
    private String streetName;

    @Column(name="street_number")
    private String streetNumber;

    @Column(name="zip_code")
    private String zipCode;

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
        return checkValidZipCode(location.getZipCode());
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
        if (streetNumber != null && streetNumber.length() > 0 && streetNumber.length() <= 9 && streetNumber.matches("[0-9]+|[0-9]+\\/[0-9]+")) {
            return true;
        } else {
            return false;
        }
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
        if (streetName != null && streetName.length() < 100 && streetName.length() > 0 && streetName.matches("[ a-zA-Z]+")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the name of the city is valid
     * Realistically no city name will be over 50 characters, they are also generally one or two words. Therefore, the
     * city name must be below 50 characters and have at least one character. Additionally, foreign addresses are
     * expected to be put in the English version, thus, the city must only contain letters.
     * @param city the city of the location
     * @return true if the city name is valid, false otherwise
     */
    public boolean checkValidCity(String city) {
        if (city != null && city.length() < 50 && city.length() > 0 && city.matches("[ a-zA-Z]+")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the name of the region is valid
     * Realisitcally no region name will be over 50 characters, they are also generally are one word. Therefore, the
     * region name must be below 50 characters and have at least one character. Additionally, foreign addresses are
     * expected to be put in the English version, thus, the region must only contain letters.
     * @param region the city of the location
     * @return true if the region name is valid, false otherwise
     */
    public boolean checkValidRegion(String region) {
        if (region != null && region.length() < 50 && region.length() > 0 && region.matches("[ a-zA-Z]+")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the name of the country is valid
     * Realisitcally no region name will be over 50 characters, they are also generally are one word. Therefore, the
     * country name must be below 50 characters and have at least one character. Additionally, foreign addresses are
     * expected to be put in the English version, thus, the country must only contain letters.
     * @param country the country of the location
     * @return true if the country name is valid, false otherwise
     */
    public boolean checkValidCountry(String country) {
        if (country != null && country.length() < 50 && country.length() > 0 && country.matches("[ a-zA-Z]+")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the zip code number is valid
     * America has the largest zip codes which include 9 digits and some countries like Canada use letters within there
     * zip codes. Therefore, the zip code must contain less or equal to 9 characters and above 0. Additionally, the
     * zip code must be alphanumberic.
     * @param zipCode the zip code of the location
     * @return true if the zip code number is valid, false otherwise
     */
    public boolean checkValidZipCode(String zipCode) {
        if (zipCode != null && zipCode.length() <= 9 && zipCode.length() > 0 && zipCode.matches("[a-zA-Z0-9]+")) {
            return true;
        } else {
            return false;
        }
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

    public String getZipCode() {
        return zipCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCountry(String country) {
        if (checkValidCountry(country)) {
            this.country = country;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The country must not be empty, be less then 50 characters, and only contain letters.");
        }
    }

    public void setCity(String city) {
        if (checkValidCity(city)) {
            this.city = city;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The city must not be empty, be less then 50 characters, and only contain letters.");
        }
    }

    public void setRegion(String region) {
        if (checkValidRegion(region)) {
            this.region = region;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The region must not be empty, be less then 50 characters, and only contain letters.");
        }
    }

    public void setStreetName(String streetName) {
        if (checkValidStreetName(streetName)) {
            this.streetName = streetName;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The region must not be empty, be less then 100 characters, and only contain letters.");
        }
    }

    public void setStreetNumber(String streetNumber) {
        if (checkValidStreetNumber(streetNumber)) {
            this.streetNumber = streetNumber;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The street number must be an number less than 1,000 and above 0.");
        }
    }

    public void setZipCode(String zipCode) {
        if (checkValidZipCode(zipCode)) {
            this.zipCode = zipCode;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The zip code must be a letter or number, be less than 10 characters long, and at least one character long.");
        }
    }

    /**
     * This class uses the builder pattern to construct an instance of the Location class
     */
    public static class Builder {

        private  String country;
        private  String city;
        private String suburb;
        private String region;
        private String streetName;
        private String streetNumber;
        private String zipCode;

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
         * Set the builder's suburb.
         * @param suburb A string representing a suburb.
         * @return Builder with suburb parameter set.
         */
        public Builder inSuburb(String suburb) {
            this.suburb = suburb;
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
         * Set the builder's zip code.
         * @param zipCode A string representing a zip code.
         * @return Builder with zip code parameter set.
         */
        public Builder withZipCode(String zipCode)  {
            this.zipCode = zipCode;
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
            location.setCity(this.city);
            location.setStreetName(this.streetName);
            location.setStreetNumber(this.streetNumber);
            location.setZipCode(this.zipCode);
            return location;
        }

    }
}
