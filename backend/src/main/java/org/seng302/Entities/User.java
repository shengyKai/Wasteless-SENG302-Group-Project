/* Subtype of Account for individual users */
package org.seng302.Entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.minidev.json.JSONObject;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Entity
public class User extends Account {

    private String firstName;
    private String middleName;
    private String lastName;
    private String nickname;
    private String bio;
    private Date dob;
    private String phNum;
    private String address;
    private Date created;
    private String role;

    /* Matches:
    123-456-7890
    (123) 456-7890
    123 456 7890
    123.456.7890
    +91 (123) 456-7890
     */
    String phoneRegex = "^(\\+\\d{1,2}\\s)?\\(?\\d{1,3}\\)?[\\s.-]?\\d{3,4}[\\s.-]?\\d{3,4}$";
    // Allows letters, numbers and selected symbols then 1 @ then some amount of other characters
    // Todo: this regex does not match the example data from the API spec. Update regex and tests
    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";


    protected User(){};

    /**
     * Returns users first name
     * @return firstName
     */
    @Column(nullable = false)
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the users first name
     * Not Null
     * @param firstName users first name
     */
    public void setFirstName(String firstName) {
        if (firstName != null && firstName.length() > 0 && firstName.length() <= 16 && firstName.matches("[ a-zA-Z]+")) {
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
        if (middleName == null || (middleName.length() > 0 && middleName.length() <= 16 && middleName.matches("[ a-zA-Z]+"))) {
            this.middleName = middleName;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The middle name must not be empty, be less then 16 characters, and only contain letters.");
        }
    }

    /**
     * Returns users last name
     * @return lastName
     */
    @Column(nullable = false)
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets users last name
     * Not Null
     * @param lastName users surname
     */
    public void setLastName(String lastName) {
        if (lastName != null && lastName.length() > 0 && lastName.length() <= 16 && lastName.matches("[ a-zA-Z]+")) {
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
        if (nickname == null || (nickname.length() > 0 && nickname.length() <= 16 && nickname.matches("[ a-zA-Z]*"))) {
            this.nickname = nickname;
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
    //Todo Discuss with team about what characters should be allowed in the BIO
    public void setBio(String bio) {
        if (bio == null || (bio.length() > 0 && bio.length() <= 255 && bio.matches("[ a-zA-Z]*"))) {
            this.bio = bio;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The bio must be less than 255 characters long, and only contain letters.");
        }
    }

    /**
     * Returns the users date of birth
     * @return dob
     */
    @Column(nullable = false)
    @JsonProperty("dateOfBirth")
    public Date getDob() {
        return dob;
    }

    /**
     * Sets the users date of birth
     * Not Null
     * @param dob date of birth (used to verify age)
     */
    public void setDob(Date dob) {
        if (dob != null) {
            this.dob = dob;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your date of birth has been entered incorrectly");
        }
    }

    /**
     * Returns the users phone number
     * @return phNum
     */
    @JsonProperty("phoneNumber")
    public String getPhNum() {
        return phNum;
    }

    /**
     * Sets the users phone number, must be in proper ph num format
     * @param phNum users contact number
     */
    public void setPhNum(String phNum) {
        boolean validPhone = false;
        if (phNum == null || Pattern.matches(phoneRegex, phNum)) {
            validPhone = true;
        }
            if (validPhone) {
                this.phNum = phNum;
            } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your phone number has been entered incorrectly");
            }
        }


    /**
     * Gets the users country, city, street, house number etc as string
     * @return address
     */
    @Column(nullable = false)
    public String getAddress() {
    return this.address;
    }

    /**
     * Sets the users home address
     * Not Null
     * @param address where the user lives/provides items from
     */
    public void setAddress(String address) {
        if (address != null && address.length() > 0 && address.length() <= 255) {
            this.address = address;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your address has been entered incorrectly");
        }
    }


    /**
     * Date the account was created
     * @return Date the account was created
     */
    @ReadOnlyProperty
    public Date getCreated(){
        return this.created;
    }

    /**
     * Record the date when the account is created
     * @param date of account creation
     */
    public void setCreated(Date date){
        this.created = date;
    }

    /**
     * Authority within the system, eg: admin status and what businesses they are associated with
     * @return role
     */
    public String getRole(){
        return this.role;
    }

    /**
     * Change the description of their status within the system
     * @param role admin status and what businesses they are associated with
     */
    public void setRole(String role){
        this.role=role;
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
     * This method constructs a JSON representation of the user's public details. These are their id number,
     * first name, middle name, last name, nickname, email, bio and date the account was created.
     * @return JSONObject with attribute name as key and attribute value as value.
     */
    // Todo: Add city, region and country parts of address once parsing address string is done.
    // Todo: Replace email with profile picture once profile pictures added.
    public JSONObject constructPublicJson() {
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("id", getUserID().toString());
        attributeMap.put("firstName", firstName);
        attributeMap.put("middleName", middleName);
        attributeMap.put("lastName", lastName);
        attributeMap.put("nickname", nickname);
        attributeMap.put("email", getEmail());
        attributeMap.put("bio", bio);
        attributeMap.put("created", created.toString());
        attributeMap.put("homeAddress", getAddress());
        return new JSONObject(attributeMap);
    }

    /**
     * This method constructs a JSON representation of the user's private details.
     * @return JSONObject with attribute name as key and attribute value as value.
     */
    // Todo: Once businesses are done, add businessesAdministered
    public JSONObject constructPrivateJson() {
        //Map<String, String> attributeMap = constructPublicJson();
        JSONObject json = constructPublicJson();
        json.appendField("dateOfBirth", dob.toString());
        json.appendField("phoneNumber", phNum);
        json.appendField("role", role);
        //json.appendField("businessesAdministered", businessesAdministered);
        return json;
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
        private Date dob;
        private String phNum;
        private String address;
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
         * @param dobString a string representing the user's date of birth in format yyyy-MM-dd.
         * @return Builder with date of birth set.
         */
        public Builder withDob(String dobString) throws ParseException {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            this.dob = dateFormat.parse(dobString);
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
        public Builder withAddress(String address) {
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
            user.setCreated(new Date(System.currentTimeMillis()));
            user.setRole("user");
            return user;
        }

    }
}