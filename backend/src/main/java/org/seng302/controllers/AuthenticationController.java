package org.seng302.controllers;

import org.mindrot.jbcrypt.BCrypt;

public class AuthenticationController {
    /**
     * Takes a given plaintext password and hashes it with BCrypt
     * @param password The plaintext password
     * @return The hashed version of the plaintext password
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
