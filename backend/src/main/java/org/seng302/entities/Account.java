/* Parent class for all kinds of accounts */
package org.seng302.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.seng302.exceptions.EmailInUseException;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.PasswordAuthenticator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Account {
    private String email;
    private String authenticationCode;
    private Long userID;
    private boolean isDGAA;
    // Allows letters, numbers and selected symbols then 1 @ then some amount of other characters
    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";

    /**
     * Returns email associated with the account, also works as username
     * @return email
     */
    @Column(unique = true, nullable = false)
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email to be associated with the account
     * Not Null
     * @param email to be associated with account and used as username
     */
    public void setEmail(String email) {
        boolean validEmail = (email != null && Pattern.matches(emailRegex, email));
        if (validEmail && !email.trim().isEmpty() && email.length() <= 100) {
            this.email = email;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email must not be empty and must consist of an email prefix, a @ symbol and a domain, and less than 100 char");
        }
    }

    /**
     * This method checks whether there is a user in the repository with the given email and throws a EmailInUseException
     * if there is.
     * @param email The email to search the repository for.
     * @param repository Repository containing all registered users.
     */
    public static void checkEmailUniqueness(String email, UserRepository repository) {
        if (repository.findByEmail(email) != null) {
            throw new EmailInUseException();
        }
    }

    /**
     * Returns password of the account
     * @return password (Hashed)
     */
    @Column(nullable = false)
    @JsonIgnore
    public String getAuthenticationCode() {
        return authenticationCode;
    }

    /**
     * Sets the authentication code of the account from a given password
     * Not Null
     * @param password to be hashed and stored
     */
    public void setAuthenticationCodeFromPassword(String password) {
        if (password != null && !password.trim().isEmpty() && password.length() >= 7 && password.length() <= 32 && password.matches(".*[a-zA-Z].*") && password.matches(".*[0-9].*")) {
            try {
                this.authenticationCode = PasswordAuthenticator.generateAuthenticationCode(password);

            } catch (NoSuchAlgorithmException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate authentication code. Try again later");
            }
        } else {
            // Password cannot be null or empty
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The password must be at least 7 characters long and no longer than 32.");
        }
    }

    /**
     * Sets the authentication code of the account as equal to the given authentication code. This method is used by the
     * JPA repository. For intializing accoutn use setAuthenticationCodeFromPassword instead.
     * @param authenticationCode
     */
    public void setAuthenticationCode(String authenticationCode) {
        if (authenticationCode.matches("[a-f0-9]*") && authenticationCode.length() == 64) {
            this.authenticationCode = authenticationCode;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a vaild authentication code.");
        }
    }

    /**
     * Returns primary key of where it is in the tables
     * @return userID
     */
    @Id
    @GeneratedValue
    @JsonProperty("id")
    public Long getUserID() {
        return userID;
    }

    /**
     * Sets the userID to be the primary key in the table the account is stored in for future reference
     * @param userID unique number of account in database
     */
    public void setUserID(Long userID) {
        this.userID = userID;
    }

    /**
     * Token of if they have admin rights, certain actions will check this before happening
     * @return admin
     */
    public boolean isIsDGAA() {
        return isDGAA;
    }

    /**
     * Set whether or not user has administrative powers
     * @param DGAA whether or not user is an admin
     */
    @JsonIgnore
    public void setIsDGAA(boolean isDGAA) {
        this.isDGAA = isDGAA;
    }
}