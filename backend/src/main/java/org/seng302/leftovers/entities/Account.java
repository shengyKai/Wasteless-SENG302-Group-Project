/* Parent class for all kinds of accounts */
package org.seng302.leftovers.entities;

import org.seng302.leftovers.dto.user.UserRole;
import org.seng302.leftovers.exceptions.ConflictResponseException;
import org.seng302.leftovers.exceptions.InternalErrorResponseException;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.PasswordAuthenticator;

import javax.persistence.*;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userID;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String authenticationCode;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    protected UserRole role;
    // Allows letters, numbers and selected symbols then 1 @ then some amount of other characters
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    /**
     * Returns email associated with the account, also works as username
     * @return email
     */

    public String getEmail() {
        return email;
    }

    /**
     * Sets the email to be associated with the account
     * Not Null
     * @param email to be associated with account and used as username
     */
    public void setEmail(String email) {
        boolean validEmail = (email != null && Pattern.matches(EMAIL_REGEX, email));
        if (validEmail && !email.trim().isEmpty() && email.length() <= 100) {
            this.email = email;
        } else {
            throw new ValidationResponseException(
            "Email must not be empty and must consist of an email prefix, a @ symbol and a domain, and less than 100 char");
        }
    }

    /**
     * This method checks whether there is a user in the repository with the given email and throws a ConflictResponseException
     * if there is.
     * @param email The email to search the repository for.
     * @param repository Repository containing all registered users.
     */
    public static void checkEmailUniqueness(String email, UserRepository repository) {
        if (repository.findByEmail(email) != null) {
            throw new ConflictResponseException("Email already in use");
        }
    }

    /**
     * Returns password of the account
     * @return password (Hashed)
     */
    public String getAuthenticationCode() {
        return authenticationCode;
    }

    /**
     * Sets the authentication code of the account from a given password
     * Not Null
     * @param password to be hashed and stored
     */
    public void setAuthenticationCodeFromPassword(String password) {
        if (password != null && !password.trim().isEmpty() && password.length() >= 7 && password.length() <= 32 && password.matches(".*[\\p{L}!@#$%^&*()-].*") && password.matches(".*[0-9].*")) {
            try {
                this.authenticationCode = PasswordAuthenticator.generateAuthenticationCode(password);

            } catch (NoSuchAlgorithmException e) {
                throw new InternalErrorResponseException("Could not generate authentication code. Try again later", e);
            }
        } else {
            // Password cannot be null or empty
            throw new ValidationResponseException("The password must be at least 7 characters long and no longer than 32.");
        }
    }

    /**
     * Sets the authentication code of the account as equal to the given authentication code. This method is used by the
     * JPA repository. For initialising account use setAuthenticationCodeFromPassword instead.
     * @param authenticationCode
     */
    public void setAuthenticationCode(String authenticationCode) {
        if (authenticationCode.matches("[a-f0-9]*") && authenticationCode.length() == 64) {
            this.authenticationCode = authenticationCode;
        } else {
            throw new ValidationResponseException("Not a valid authentication code.");
        }
    }

    /**
     * Returns primary key of where it is in the tables
     * @return userID
     */
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
     * Authority within the system, eg: admin status and what businesses they are associated with
     * @return role
     */
    public UserRole getRole(){
        return this.role;
    }

    /**
     * Change the description of their status within the system
     * @param role admin status and what businesses they are associated with
     */
    public void setRole(UserRole role){
        this.role = role;
    }

}