package org.seng302.Entities;

import net.minidev.json.JSONObject;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.seng302.Entities.User;
import org.seng302.Exceptions.EmailInUseException;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.seng302.Tools.PasswordAuthenticator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserTests {
    public User testUser;
    private User.Builder testBuilder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessRepository businessRepository;

    @BeforeEach
    public void setup() throws ParseException {
        // Example user object for tests
        testUser = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("nick")
                .withEmail("here@testing")
                .withPassword("12345678")
                .withBio("g")
                .withDob("2021-03-11")
                .withPhoneNumber("123-456-7890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void emailTest() {
        testUser.setEmail("Hi_123@testing.com");
        assertEquals(testUser.getEmail(), "Hi_123@testing.com");

        List<String> badEmails = new ArrayList<String>();
        String badEmail1 = "$%#@qwer";
        String badEmail2 = " ";
        String badEmail3 = ";DROPTABLE users";
        String badEmail4 = "No Spaces@cheekybugger.co.au";
        badEmails.add(badEmail1);
        badEmails.add(badEmail2);
        badEmails.add(null);
        badEmails.add(badEmail3);
        badEmails.add(badEmail4);
        for (String email : badEmails) {
            try {
                testUser.setEmail(email);
            } catch (ResponseStatusException | NullPointerException e) {

            }
            assertEquals(testUser.getEmail(), "Hi_123@testing.com");
            //None of these should work, so email will be unchanged from last success case
        }
    }

    //Test hashing - not implemented yet
    @Test @Ignore
    public void passwordTest() {

    }

    @Test
    void phoneTest() {
        List<String> goodPhones = new ArrayList<>();
        String goodPhone1 = "123-456-7890";
        String goodPhone2 = "(123) 456-7890";
        String goodPhone3 = "123 456 7890";
        String goodPhone4 = "123.456.7890";
        String goodPhone5 = "+91 (123) 456-7890";
        String goodPhone6 = "+64 3 555 0129";
        goodPhones.add(goodPhone1);
        goodPhones.add(goodPhone2);
        goodPhones.add(goodPhone3);
        goodPhones.add(goodPhone4);
        goodPhones.add(goodPhone5);
        goodPhones.add(goodPhone6);

        for (String phone : goodPhones) {
            testUser.setPhNum(phone);
            assertEquals(testUser.getPhNum(), phone);
        }

        List<String> badPhones = new ArrayList<>();
        String badPhone1 = "asdf";
        String badPhone2 = "67--123456";
        String badPhone3 = " ";
        badPhones.add(badPhone1);
        badPhones.add(badPhone2);
        badPhones.add(badPhone3);

        for (String phone : badPhones) {
            try {
                testUser.setPhNum(phone);
            } catch (ResponseStatusException e) {}
            assertEquals(goodPhone6, testUser.getPhNum()); // Bad wont change, so last good phone is current
        }
    }


    @Test @Ignore
    public void createUserTest() {

    }

    /**
     * Checks several real life first names will be set as the user's first name
     */
    @Test
    public void checkValidFirstNames() {
        String[] validFirstNames = { "Connor", "Ella", "Johnny", "Richard", "Ned", "Jefferson", "Jackson", "Oliver" };
        for (String firstName : validFirstNames) {
            testUser.setFirstName(firstName);
            assertEquals(testUser.getFirstName(), firstName);
        }
    }

    /**
     * Checks an empty first name will not be set as the user's first name
     */
    @Test
    public void checkInvalidFirstNameEmpty() {
        try {
            testUser.setFirstName("");
            fail("A Forbidden exception was expected, but not thrown");
        } catch (ResponseStatusException expectedException) { }
    }

    /**
     * Checks several first names that are too long (above 16 letters long) will not be set as the user's first name
     */
    @Test
    public void checkInvalidFirstNameTooLong() {
        String[] invalidFirstNames = { "HippoTooLongPotamus", "Connnnnnnnnnnnnnnnnnnnnor", "MrsMagicalMagical" };
        for (String firstName : invalidFirstNames) {
            try {
                testUser.setFirstName(firstName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several first names with numbers in them will not be set as the user's first name
     */
    @Test
    public void checkInvalidFirstNameNumbers() {
        String[] invalidFirstNames = { "C0nn0r", "E11a", "123456789", "1", "0", "Mohammad1" };
        for (String firstName : invalidFirstNames) {
            try {
                testUser.setFirstName(firstName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several first names with characters in them will not be set as the user's first name
     */
    @Test
    public void checkInvalidFirstNameCharacters() {
        String[] invalidFirstNames = { "C#nn#r", "E!!@", "!@#$%^&*()", "!", "@", "Mohammad*" };
        for (String firstName : invalidFirstNames) {
            try {
                testUser.setFirstName(firstName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several real life middle names will be set as the user's middle name
     */
    @Test
    public void checkValidMiddleNames() {
        String[] validMiddleNames = { "Connor", "Ella", "Johnny", "Richard", "Ned", "Jefferson", "Jackson", "Oliver" };
        for (String middleName : validMiddleNames) {
            testUser.setMiddleName(middleName);
            assertEquals(testUser.getMiddleName(), middleName);
        }
    }

    /**
     * Checks an empty middle name will not be set as the user's middle name
     */
    @Test
    public void checkInvalidMiddleNameEmpty() {
        try {
            testUser.setMiddleName("");
            fail("A Forbidden exception was expected, but not thrown");
        } catch (ResponseStatusException expectedException) { }
    }

    /**
     * Checks several middle names that are too long (above 16 letters long) will not be set as the user's middle name
     */
    @Test
    public void checkInvalidMiddleNameTooLong() {
        String[] invalidMiddleNames = { "HippoTooLongPotamus", "Connnnnnnnnnnnnnnnnnnnnor", "MrsMagicalMagical" };
        for (String middleName : invalidMiddleNames) {
            try {
                testUser.setMiddleName(middleName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several middle names with numbers in them will not be set as the user's middle name
     */
    @Test
    public void checkInvalidMiddleNameNumbers() {
        String[] invalidMiddleNames = { "C0nn0r", "E11a", "123456789", "1", "0", "Mohammad1" };
        for (String middleName : invalidMiddleNames) {
            try {
                testUser.setMiddleName(middleName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several middle names with characters in them will not be set as the user's middle name
     */
    @Test
    public void checkInvalidMiddleNameCharacters() {
        String[] invalidMiddleNames = { "C#nn#r", "E!!@", "!@#$%^&*()", "!", "@", "Mohammad*" };
        for (String middleName : invalidMiddleNames) {
            try {
                testUser.setMiddleName(middleName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several real life last names will be set as the user's last name
     */
    @Test
    public void checkValidLastNames() {
        String[] validLastNames = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis" };
        for (String lastName : validLastNames) {
            testUser.setLastName(lastName);
            assertEquals(testUser.getLastName(), lastName);
        }
    }

    /**
     * Checks an empty last name will not be set as the user's last name
     */
    @Test
    public void checkInvalidLastNameEmpty() {
        try {
            testUser.setLastName("");
            fail("A Forbidden exception was expected, but not thrown");
        } catch (ResponseStatusException expectedException) { }
    }

    /**
     * Checks several last names that are too long (above 16 letters long) will not be set as the user's last name
     */
    @Test
    public void checkInvalidLastNameTooLong() {
        String[] invalidLastNames = { "HippoTooLongPotamus", "Connnnnnnnnnnnnnnnnnnnnor", "MrsMagicalMagical" };
        for (String lastName : invalidLastNames) {
            try {
                testUser.setLastName(lastName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several last names with numbers in them will not be set as the user's last name
     */
    @Test
    public void checkInvalidLastNameNumbers() {
        String[] invalidLastNames = { "Sm1th", "J0hns0n", "123456789", "1", "0", "Mohammad1" };
        for (String lastName : invalidLastNames) {
            try {
                testUser.setLastName(lastName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several last names with characters in them will not be set as the user's last name
     */
    @Test
    public void checkInvalidLastNameCharacters() {
        String[] invalidLastNames = { "Sm!th", "J#hn$#n", "!@#$%^&*()", "!", "@", "Mohammad*" };
        for (String lastName : invalidLastNames) {
            try {
                testUser.setLastName(lastName);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several nicknames will be set as the user's nickname
     */
    @Test
    public void checkValidNicknames() {
        String[] validNicknames = { "Peach", "Rose", "Built Different", "God", "That Guy", "Officer", "Pebble" };
        for (String nickname : validNicknames) {
            testUser.setNickname(nickname);
            assertEquals(testUser.getNickname(), nickname);
        }
    }

    /**
     * Checks several nicknames that are too long (above 16 letters long) will not be set as the user's nickname
     */
    @Test
    public void checkInvalidNicknameTooLong() {
        String[] invalidNicknames = { "HippoTooLongPotamus", "Connnnnnnnnnnnnnnnnnnnnor", "MrsMagicalMagical" };
        for (String nickname : invalidNicknames) {
            try {
                testUser.setNickname(nickname);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several nicknames with numbers in them will not be set as the user's nickname
     */
    @Test
    public void checkInvalidNicknameNumbers() {
        String[] invalidNicknames = { "Sm1th", "J0hns0n", "123456789", "1", "0", "Mohammad1" };
        for (String nickname : invalidNicknames) {
            try {
                testUser.setNickname(nickname);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several nicknames with characters in them will not be set as the user's nickname
     */
    @Test
    public void checkInvalidNicknameCharacters() {
        String[] invalidNicknames = { "Sm!th", "J#hn$#n", "!@#$%^&*()", "!", "@", "Mohammad*" };
        for (String nickname : invalidNicknames) {
            try {
                testUser.setNickname(nickname);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several bios will be set as the user's bio
     */
    @Test
    public void checkValidBio() {
        String[] validBios = { "I am a happy person when I am not studying",
                "I am a University student meaning I have no free time",
                "Do you like cats cause I like cats" };
        for (String bio : validBios) {
            testUser.setBio(bio);
            assertEquals(testUser.getBio(), bio);
        }
    }

    /**
     * Checks several bios that are too long (above 255 letters long) will not be set as the user's bio
     */
    @Test
    public void checkInvalidBioTooLong() {
        String[] invalidBios = { "This is the story of a student hoping one day to become a developer he sat here writing this long sentence hoping to reach exactly two hundred and fifty six characters however this was a challenge if he wanted to make a sentence that would read well nicely",
        "This is the story of a student hoping one day to become a developer he sat here writing this long sentence hoping to reach exactly two hundred and fifty six characters however this was a challenge if he wanted to make a sentence that would read well nicely This is the story of a student hoping one day to become a developer he sat here writing this long sentence hoping to reach exactly two hundred and fifty six characters however this was a challenge if he wanted to make a sentence that would read well nicely"};
        for (String bio : invalidBios) {
            try {
                testUser.setBio(bio);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several bios with numbers in them will not be set as the user's bio
     */
    @Test
    public void checkInvalidBioNumbers() {
        String[] invalidBios = { "I am a happy p3rs0n when 1 am n0t study1ng",
                "1 am a Un1vers1ty stud3nt meaning I have n0 fr33 t1m3",
                "D0 y0u l1k3 cats caus3 1 l1k3 cats", "0", "1", "0123456789" };
        for (String bio : invalidBios) {
            try {
                testUser.setBio(bio);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several bios with characters in them will not be set as the user's bio
     */
    @Test
    public void checkInvalidBioCharacters() {
        String[] invalidBios = { "! @m @ h@ppy per$on when ! @m not study!ng",
                "! @m @ Un!ver$ity $tudent meaning I have no &ree ^ime",
                "Do you li%e ca(s cause ) l*ke cats", "!", "@", "!@#$%^&^*(*)" };
        for (String bio : invalidBios) {
            try {
                testUser.setBio(bio);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several address strings will be set as the user's address
     */
    @Test
    public void checkValidAddress() {
        String[] validAddresses = { "20,Elizabeth Street,Christchurch,New Zealand,Canterbury,8041",
                "10,Made Up Street,Los Angeles,United States of Not Real,Fakeland,99999",
                "49,You Would Not,Believe,Your,Eyes,11111" };
        for (String address : validAddresses) {
            testUser.setAddress(address);
            Location location = Location.covertAddressStringToLocation(address);
            assertEquals(testUser.getAddress(), location);
        }
    }

    /**
     * Checks an empty address string will not be set as the user's address
     */
    @Test
    public void checkInvalidAddressEmpty() {
        try {
            testUser.setAddress("");
            fail("A Forbidden exception was expected, but not thrown");
        } catch (ResponseStatusException expectedException) { }
    }

    /**
     * Checks several address strings that are too long (above 255 letters long) will not be set as the user's address
     */
    @Test
    public void checkInvalidAddressTooLong() {
        String[] invalidAddresses = { "This is the story of a student hoping one day to become a developer he sat here writing this long sentence hoping to reach exactly two hundred and fifty six characters however this was a challenge if he wanted to make a sentence that would read well nicely",
                "This is the story of an address hoping one day to become a developer he sat here writing this long sentence hoping to reach exactly two hundred and fifty six characters however this was a challenge if he wanted to make a sentence that would read well nicely This is the story of a student hoping one day to become a developer he sat here writing this long sentence hoping to reach exactly two hundred and fifty six characters however this was a challenge if he wanted to make a sentence that would read well nicely"};
        for (String address : invalidAddresses) {
            try {
                testUser.setAddress(address);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several passwords will be set as the user's password
     */
    @Test @Ignore
    public void checkValidPassword() throws NoSuchAlgorithmException {
        String[] validPasswords = { "SAASDJKAasdasdsa$*#", "asdjaskdj383", "asjdksajk&&&",
                "ASJDKLASJLKDJASKLDJK234567890123", "SKLDJASKD*(*(@*(#@*(8238283999" };
        for (String password : validPasswords) {
            testUser.setAuthenticationCodeFromPassword(password);
            assertEquals(PasswordAuthenticator.generateAuthenticationCode(password), testUser.getAuthenticationCode());
        }
    }

    /**
     * Checks an empty password will not be set as the user's password
     */
    @Test
    public void checkInvalidPasswordEmpty() {
        try {
            testUser.setAuthenticationCodeFromPassword("");
            fail("A Forbidden exception was expected, but not thrown");
        } catch (ResponseStatusException expectedException) { }
    }

    /**
     * Checks several passwords that are too short (below 8 characters long) will not be set as the user's password
     */
    @Test
    public void checkInvalidPasswordTooShort() {
        String[] invalidPasswords = { "1", "12", "123", "1234", "12345", "123456", "1234567", "YaBoi#d", "@", "HEy" };
        for (String password : invalidPasswords) {
            try {
                testUser.setAuthenticationCodeFromPassword(password);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Checks several passwords that are too long (above 64 characters long) will not be set as the user's password
     */
    @Test
    public void checkInvalidPasswordTooLong() {
        String[] invalidPasswords = { "this password will be exactly 65 characters long seeeeeeeeeeeeeee", "" +
                "asjdklasjkldjaslkdjlasjdklasjlkd^*&$#(*&#*(W$&(*#&#*(&(*3798427329847293874982378932480923490",
                "asdjkl;asdasjkdsjakljaslkdjsa k        37498823094*()(#*()*($)#)*()#$*(#)$*()$#*DKLDJSL"};
        for (String password : invalidPasswords) {
            try {
                testUser.setAuthenticationCodeFromPassword(password);
                fail("A Forbidden exception was expected, but not thrown");
            } catch (ResponseStatusException expectedException) { }
        }
    }

    /**
     * Verify that checkEmailUniqueness does not throw an exception when called with an email that has not been added
     * to the user repository.
     */
    @Test
    public void checkEmailUniquenessUniqueTest() {
        String testEmail = "johnsmith99@gmail.com";
        when(userRepository.findByEmail(testEmail)).thenAnswer(
                (Answer) invocation -> null);
        try {
            User.checkEmailUniqueness(testEmail, userRepository);
        } catch (Exception e) {
            fail("Exception should not be thrown when checkEmailUniqueness is called with an email which " +
                    "has not been added to the user repository");
        }
    }

    /**
     * Verify that constructPublicJSON returns a JSON with the public attributes present.
     */
    @Test
    public void constructPublicJsonPublicAttributesPresentTest() {
        testUser.setUserID(1L);
        JSONObject json = testUser.constructPublicJson();
        assertTrue(json.containsKey("id"));
        assertTrue(json.containsKey("firstName"));
        assertTrue(json.containsKey("middleName"));
        assertTrue(json.containsKey("lastName"));
        assertTrue(json.containsKey("nickname"));
        assertTrue(json.containsKey("email"));
        assertTrue(json.containsKey("bio"));
        assertTrue(json.containsKey("created"));
    }

    /**
     * Verify that constructPublicJson returns a JSON with none of the hidden attributes present.
     */
    @Test
    public void constructPublicJsonHiddenAttributesNotPresentTest() {
        testUser.setUserID(1L);
        JSONObject json = testUser.constructPublicJson();
        json.remove("id");
        json.remove("firstName");
        json.remove("middleName");
        json.remove("lastName");
        json.remove("nickname");
        json.remove("email");
        json.remove("bio");
        json.remove("created");
        json.remove("dateOfBirth");
        json.remove("homeAddress");
        assertTrue(json.isEmpty());
    }

    /**
     * Verify that when constructPublicJson is called on a User which has none of its attributes set to null,
     * all of the attributes in the JSON have the expected value.
     */
    @Test
    public void constructPublicJsonNoAttributesNullTest() {
        testUser.setUserID(1L);
        JSONObject json = testUser.constructPublicJson();
        assertEquals(testUser.getUserID().toString(), json.getAsString("id"));
        assertEquals(testUser.getFirstName(), json.getAsString("firstName"));
        assertEquals(testUser.getMiddleName(), json.getAsString("middleName"));
        assertEquals(testUser.getLastName(), json.getAsString("lastName"));
        assertEquals(testUser.getNickname(), json.getAsString("nickname"));
        assertEquals(testUser.getBio(), json.getAsString("bio"));
        assertEquals(testUser.getEmail(), json.getAsString("email"));
        assertEquals(testUser.getCreated().toString(), json.getAsString("created"));
    }

    /**
     * Verify that when constructPublicJson is called on a User which has its optional attributes set to null,
     * all of the attributes in the JSON have the expected value.
     */
    @Test
    public void constructPublicJsonOptionalAttributesNullTest() {
        testUser.setUserID(1L);
        testUser.setMiddleName(null);
        testUser.setNickname(null);
        testUser.setBio(null);
        JSONObject json = testUser.constructPublicJson();
        assertEquals(testUser.getUserID().toString(), json.getAsString("id"));
        assertEquals(testUser.getFirstName(), json.getAsString("firstName"));
        assertEquals(testUser.getMiddleName(), json.getAsString("middleName"));
        assertEquals(testUser.getLastName(), json.getAsString("lastName"));
        assertEquals(testUser.getNickname(), json.getAsString("nickname"));
        assertEquals(testUser.getBio(), json.getAsString("bio"));
        assertEquals(testUser.getEmail(), json.getAsString("email"));
        assertEquals(testUser.getCreated().toString(), json.getAsString("created"));
    }

    /**
     * Verify that checkEmailUniqueness throws an EmailInUseException when called with an email that has been added to
     * the user repository.
     */
    @Test
    public void checkEmailUniquenessNonUniqueTest() throws ParseException {
        String testEmail = "johnsmith99@gmail.com";
        User testUser = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        when(userRepository.findByEmail(testEmail)).thenAnswer(
                (Answer) invocation -> testUser);
        assertThrows(EmailInUseException.class, () -> {
            User.checkEmailUniqueness(testEmail, userRepository);
        });
    }

    /**
     * Verify that when user is instantiated, its created attribute is set to the current system time.
     */
    @Test
    public void setCreatedTest() {
        Date testDate = new Date(System.currentTimeMillis());
        User testUser = testBuilder.build();
        assertEquals(testDate, testUser.getCreated());
    }

    /**
     * Verify that when User.Builder.build() is used without setting first name a ResponseStatusException is thrown
     */
    @Test
    public void buildWithoutFirstNameTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting last name a ResponseStatusException is thrown
     */
    @Test
    public void buildWithoutLastNameTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting email a ResponseStatusException is thrown
     */
    @Test
    public void buildWithoutEmailTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting password a ResponseStatusException is thrown
     */
    @Test
    public void buildWithoutPasswordTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting home address a ResponseStatusException is thrown
     */
    @Test
    public void buildWithoutAddressTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129");
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build() is used without setting date of birth a ResponseStatusException is thrown
     */
    @Test
    public void buildWithoutDateOfBirthTest() {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        assertThrows(ResponseStatusException.class, testBuilder::build);
    }

    /**
     * Verify that when User.Builder.build is used without setting a middle name, a user is constructed with their middle
     * name set to null
     */
    @Test
    public void buildWithoutMiddleNameTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        User testUser = testBuilder.build();
        assertNull(testUser.getMiddleName());
    }

    /**
     * Verify that when User.Builder.build is used without setting a nickname, a user is constructed with their nickname
     * set to null
     */
    @Test
    public void buildWithoutNickNameTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        User testUser = testBuilder.build();
        assertNull(testUser.getNickname());
    }

    /**
     * Verify that when User.Builder.build is used without setting a bio, a user is constructed with their bio set to null
     */
    @Test
    public void buildWithoutBioTest() throws ParseException {
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        User testUser = testBuilder.build();
        assertNull(testUser.getBio());
    }

    /**
     * Verify that when User.Builder.build is used without setting a phone number, a user is constructed with their phone
     * number set to null
     */
    @Test
    public void buildWithoutPhoneNumberTest() throws ParseException{
        User.Builder testBuilder = new User.Builder()
                .withFirstName("Joe")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"));
        User testUser = testBuilder.build();
        assertNull(testUser.getPhNum());
    }

    /**
     * Test that when User.Builder.build is called with the builder's first name set, the first name of the user will
     * be the same as the one set in the builder.
     */
    @Test
    public void buildWithFirstNameTest() {
        User user = testBuilder.build();
        assertEquals("Joe", user.getFirstName());
    }

    /**
     * Test that when User.Builder.build is called with the builder's middle name set, the middle name of the user will
     * be the same as the one set in the builder.
     */
    @Test
    public void buildWithMiddleNameTest() {
        User user = testBuilder.build();
        assertEquals("Hector", user.getMiddleName());
    }

    /**
     * Test that when User.Builder.build is called with the builder's last name set, the last name of the user will
     * be the same as the one set in the builder.
     */
    @Test
    public void buildWithLastNameTest() {
        User user = testBuilder.build();
        assertEquals("Smith", user.getLastName());
    }

    /**
     * Test that when User.Builder.build is called with the builder's nickname set, the nickname of the user will
     * be the same as the one set in the builder.
     */
    @Test
    public void buildWithNickNameTest() {
        User user = testBuilder.build();
        assertEquals("Jonny", user.getNickname());
    }

    /**
     * Test that when User.Builder.build is called with the builder's email set, the email of the user will
     * be the same as the one set in the builder.
     */
    @Test
    public void buildWithEmailTest() {
        User user = testBuilder.build();
        assertEquals("johnsmith99@gmail.com", user.getEmail());
    }

    /**
     * Test that when User.Builder.build is called with the builder's password set, the authentication code of the user
     * will be the same as the one generated from the password set in the builder.
     */
    @Test
    public void buildWithPasswordTest() throws NoSuchAlgorithmException {
        User user = testBuilder.build();
        assertEquals(PasswordAuthenticator.generateAuthenticationCode("1337-H%nt3r2"), user.getAuthenticationCode());
    }

    /**
     * Test that when User.Builder.build is called with the builder's bio set, the bio of the user will be the same as
     * the one set in the builder.
     */
    @Test
    public void buildWithBioTest() {
        User user = testBuilder.build();
        assertEquals("Likes long walks on the beach", user.getBio());
    }

    /**
     * Test that when User.Builder.build is called with the builder's home address set, the home address of the user will
     * be the same as the one set in the builder.
     */
    @Test
    public void buildWithAddressTest() {
        User user = testBuilder.build();
        assertEquals(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand,Canterbury,8041"), user.getAddress());
    }

    /**
     * Test that when User.Builder.build is called with the builder's phone number set, the phone number of the user will
     * be the same as the one set in the builder.
     */
    @Test
    public void buildWithPhoneNumberTest() {
        User user = testBuilder.build();
        assertEquals("+64 3 555 0129", user.getPhNum());
    }

    /**
     * Test that when User.Builder.build is called with the builder's date of birth set, the date of birth of the user will
     * be equivalent to the one set in the builder.
     */
    @Test
    public void buildWithDobTest() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dob = dateFormat.parse("2021-03-11");
        User user = testBuilder.build();
        assertEquals(dob, user.getDob());
    }

}
