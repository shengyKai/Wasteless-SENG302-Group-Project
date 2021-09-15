/* Subtype of Account for individual users */
package org.seng302.leftovers.entities;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.dto.user.UserRole;
import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.tools.JsonTools;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Entity
public class User extends Account {

    private static final String NAME_REGEX = "[ \\p{L}\\-'.]+";

    @Column(nullable = false)
    private String firstName;

    private String middleName;

    @Column(nullable = false)
    private String lastName;

    private String nickname;

    private String bio;

    @Column(nullable = false)
    private LocalDate dob;

    private String phNum;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Location address;

    @ReadOnlyProperty
    private Instant created;

    @ManyToMany(mappedBy = "administrators", fetch = FetchType.LAZY)
    private Set<Business> businessesAdministered = new HashSet<>();

    @OneToMany(mappedBy = "primaryOwner", fetch = FetchType.LAZY)
    private Set<Business> businessesOwned = new HashSet<>();

    @OneToMany(mappedBy = "notifiedUser", fetch = FetchType.LAZY)
    private Set<Event> events = new HashSet<>();

    @ManyToMany(mappedBy = "interestedUsers", fetch = FetchType.LAZY)
    private Set<SaleItem> likedSaleItems = new HashSet<>();

    /* Matches:
    123-456-7890
    (123) 456-7890
    123 456 7890
    123.456.7890
    +91 (123) 456-7890
     */
    private static final String PHONE_REGEX = "(^[0-9]{2,3})[ ]([0-9]{4,12})$";


    protected User() {}

    /**
     * Returns users first name
     * @return firstName
     */

    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the users first name
     * Not Null
     * @param firstName users first name
     */
    public void setFirstName(String firstName) {
        if (firstName != null && firstName.length() > 0 && firstName.length() <= 32 && firstName.matches(NAME_REGEX)) {
            this.firstName = firstName;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The first name must not be empty, be less then 16 characters, and only contain letters.");
        }
    }

    /**
     * Returns users middle name
     * @return middle name of user
     */
    public String getMiddleName() {return middleName;}

    /**
     * Sets users middle name
     * Can be null
     * @param middleName
     */
    public void setMiddleName(String middleName) {
        if (middleName == null || (middleName.length() > 0 && middleName.length() <= 32 && middleName.matches(NAME_REGEX))) {
            this.middleName = middleName;
        } else if (middleName.equals("")) {
            this.middleName = null;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The middle name must not be empty, be less then 16 characters, and only contain letters.");
        }
    }

    /**
     * Returns users last name
     * @return lastName
     */

    public String getLastName() {
        return lastName;
    }

    /**
     * Sets users last name
     * Not Null
     * @param lastName users surname
     */
    public void setLastName(String lastName) {
        if (lastName != null && lastName.length() > 0 && lastName.length() <= 32 && lastName.matches(NAME_REGEX)) {
            this.lastName = lastName;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The last name must not be empty, be less then 16 characters, and only contain letters.");
        }
    }

    /**
     * Returns users preferred name
     * @return nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the users preferred nickname
     * @param nickname users preferred name
     */
    public void setNickname(String nickname) {
        if (nickname == null || (nickname.length() > 0 && nickname.length() <= 32 && nickname.matches("[ \\p{L}\\-'.]*"))) {
            this.nickname = nickname;
        } else if (nickname.equals("")) {
            this.nickname = null;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The nickname must not be empty, be less then 16 characters, and only contain letters.");
        }
    }

    /**
     * Returns the users biography
     * @return bio
     */
    public String getBio() {
        return this.bio;
    }

    /**
     * Sets the users biography - short text about themselves
     * @param bio brief description of user
     */
    public void setBio(String bio) {
        if (bio == null || (bio.length() > 0 && bio.length() <= 200 && bio.matches("[ \\p{L}0-9\\p{Punct}$]*"))) {
            this.bio = bio;
        } else if (bio.equals("")) {
            this.bio = null;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The bio must be less than 200 characters long," + 
            "and only contain letters, numbers, and valid special characters");
        }
    }

    /**
     * Returns the users date of birth
     * @return dob
     */
    public LocalDate getDob() {
        return dob;
    }

    /**
     * Sets the users date of birth
     * Not Null
     * Check the dob satisfied the condition( >= 13years)
     * Date constructor is deprecated (Date class issue)
     * LocalDate class can be used but come with time zone -- over complicated
     * @param dob date of birth (used to verify age)
     */
    public void setDob(LocalDate dob) {
        if (dob != null) {
            LocalDate now = LocalDate.now();
            LocalDate minDate = now.minusYears(13);
            
            if (dob.compareTo(minDate) < 0) {
                this.dob = dob;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must be at least 13 years old to create an account");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your date of birth has been entered incorrectly");
        }
    }

    /**
     * Returns the users phone number
     * @return phNum
     */
    public String getPhNum() {
        return phNum;
    }

    /**
     * Sets the users phone number, must be in proper ph num format
     * @param phNum users contact number
     */
    public void setPhNum(String phNum) {
        boolean validPhone = false;
        if (phNum == null || Pattern.matches(PHONE_REGEX, phNum)) {
            validPhone = true;
        }
        if (validPhone) {
            this.phNum = phNum;
        } else if (phNum.equals("")) {
            this.phNum = null;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your phone number has been entered incorrectly");
        }
    }

    /**
     * Gets the users country, city, street, house number etc as string
     * @return address
     */
    public Location getAddress() {
    return this.address;
    }

    /**
     * Sets the users home address
     * Not Null
     * @param address where the user lives/provides items from as a location object
     */
    public void setAddress(Location address) {
        if (address != null) {
            this.address = address;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your address has been entered incorrectly");
        }
    }

    /**
     * Date the account was created
     * @return Date the account was created
     */
    public Instant getCreated(){
        return this.created;
    }

    /**
     * Record the date when the account is created
     * @param date of account creation
     */
    public void setCreated(Instant date){
        this.created = date;
    }

    /**
     * Gets the set of businesses this user is the primary owner of
     * @return Set of businesses owned
     */
    public Set<Business> getBusinessesOwned() {
        return this.businessesOwned;
    }


    /**
     * Gets the set of businesses that the user is an admin of
     * @return Businesses administered
     */

    public Set<Business> getBusinessesAdministered() {
        return this.businessesAdministered;
    }


    public Set<Event> getEvents() { return this.events;}

    /**
     * Gets the set of businesses that the user is an admin of OR is the owner of
     * @return Businesses administered or owned
     */
    @Transient
    public Set<Business> getBusinessesAdministeredAndOwned() {
        Set<Business> mergedSet = new HashSet<>();
        mergedSet.addAll(getBusinessesOwned());
        mergedSet.addAll(getBusinessesAdministered());
        return mergedSet;
    }

    /**
     * ToString method override, helpful for testing
     * @return String representation of a user
     */
    @Override
    public String toString(){
        return String.format(
          "{id: %d, firstName: %s, lastName: %s}",
                this.getUserID(),
                this.firstName,
                this.lastName
        );
    }

    /**
     * Called before a user is removed from the database
     * Ensures that the User is not an owner of any Businesses.
     * If the User is an administrator for any businesses, they are removed from the administrator set for each business
     * @throws ResponseStatusException If User owns any businesses
     */
    @PreRemove
    public void preRemove() {
        if (!this.getBusinessesOwned().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete a user who is an owner of one or more businesses");
        }
        for (Business business : this.getBusinessesAdministered()) {
            business.removeAdmin(this);
        }
    }


    /**
     * This class uses the builder pattern to construct an instance of the User class
     */
    public static class Builder {

        private String firstName;
        private String middleName;
        private String lastName;
        private String nickname;
        private String email;
        private String bio;
        private LocalDate dob;
        private String phNum;
        private Location address;
        private String password;

        /**
         * Set the builder's first name. This field is required.
         * @param firstName a string representing the user's first name.
         * @return Builder with first name set.
         */
        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        /**
         * Set the builder's middle name. This field is optional.
         * @param middleName a string representing the user's middle name.
         * @return Builder with middle name set.
         */
        public Builder withMiddleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        /**
         * Set the builder's last name. This field is required.
         * @param lastName a string representing the user's last name.
         * @return Builder with last name set.
         */
        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        /**
         * Set the builder's nickname. This field is optional.
         * @param nickname a string representing the user's nickname.
         * @return Builder with nickname set.
         */
        public Builder withNickName(String nickname) {
            this.nickname = nickname;
            return this;
        }

        /**
         * Set the builder's email. This field is required.
         * @param email a string representing the user's first email.
         * @return Builder with email set.
         */
        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        /**
         * Set the builder's bio. This field is optional.
         * @param bio a string representing the user's bio.
         * @return Builder with bio set.
         */
        public Builder withBio(String bio) {
            this.bio = bio;
            return this;
        }

        /**
         * Set the builder's date of birth. This field is required.
         * @param dob a LocalDate representing the user's date of birth.
         * @return Builder with date of birth set.
         */
        public Builder withDob(LocalDate dob) {
            this.dob = dob;
            return this;
        }
        /**
         * Set the builder's date of birth. This field is required.
         * @param dobString a String representing the user's date of birth.
         * @return Builder with date of birth set.
         */
        public Builder withDob(String dobString) {
            this.dob = LocalDate.parse(dobString);
            return this;
        }

        /**
         * Set the builder's phone number. This field is optional.
         * @param phoneNumber a string representing the user's phoneNumber.
         * @return Builder with phoneNumber set.
         */
        public Builder withPhoneNumber(String phoneNumber) {
            this.phNum = phoneNumber;
            return this;
        }

        /**
         * Set the builder's address. This field is required.
         * @param address a string representing the user's address.
         * @return Builder with address set.
         */
        public Builder withAddress(Location address) {
            this.address = address;
            return this;
        }

        /**
         * Set the builder's password. This field is required.
         * @param password a string representing the user's password.
         * @return Builder with password set.
         */
        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * Construct an instance of user using the attributes from the builder.
         * @return An instance of the user class with given attributes.
         */
        public User build() {
            User user = new User();
            user.setFirstName(this.firstName);
            user.setMiddleName(this.middleName);
            user.setLastName(this.lastName);
            user.setNickname(this.nickname);
            user.setEmail(this.email);
            user.setAuthenticationCodeFromPassword(this.password);
            user.setBio(this.bio);
            user.setDob(this.dob);
            user.setPhNum(this.phNum);
            user.setAddress(this.address);
            user.setCreated(Instant.now());
            user.setRole(UserRole.USER);
            return user;
        }

    }
}
