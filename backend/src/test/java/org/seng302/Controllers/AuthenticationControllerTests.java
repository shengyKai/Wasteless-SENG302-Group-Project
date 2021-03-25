package org.seng302.Controllers;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.test.context.SpringBootTest;

import static org.seng302.Controllers.AuthenticationController.hashPassword;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AuthenticationControllerTests {

    /**
     * Tests several valid plaintext passwords to see if they match with the hashed version
     */
    @Test
    public void validPasswordHashes() {
        String[] validPasswords = { "ThisIsAGoodPassword123#", "1Pa$$1Word", "Genius9892@", "SomeonebodyOnceToldMe78",
                "askjldhaskjhjasdhkjas28983298239&**", "SomebodyOnceToldMeTheWorldGonnaRollMe23", "NOPPPPPPE$%^&*888" };
        for (String password : validPasswords) {
            String hashedPassword = hashPassword(password);
            boolean samePassword = BCrypt.checkpw(password, hashedPassword);
            assertTrue(samePassword);
        }
    }

    /**
     * Tests several pairs of passwords to make sure different strings do not hash to the same string
     */
    @Test
    public void invalidPasswordHashes() {
        String[] invalidPasswordsFirst = { "ThisIsAGoodPassword123#", "1Pa$$1Word", "Genius9892@", "SomeonebodToldMe78",
                "askjldhaskjhjasdhkjas28983298239&**", "SomebodyOnceToldMeTheWorldGonnaRollMe23", "NOPPPPPPE$%^&*888" };
        String[] invalidPasswordsSecond = { "hsjdhfjdhdkjhkj8234932", "ashdjklashjkdhaskj9892839829", "asjdkasjd$#343",
                "ashdjkashdjka4kjas", "28399458239sJKAs", "asjhdajkshdkjas47", "jkshdfkjashj7328478238@&*#@&*#&@" };
    }

}
