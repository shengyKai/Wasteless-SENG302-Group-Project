package org.seng302.Entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;


import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BusinessTests {

    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    UserRepository userRepository;

    User testUser1;
    User testUser2;
    User testUser3;
    Business testBusiness1;

    /**
     * Sets up 3 Users and 1 Business
     * User1 is primary owner of the business
     * @throws ParseException
     */
    @BeforeEach
    public void setUp() throws ParseException {
        LocalDateTime ldt = LocalDateTime.now().minusYears(15);
        String ageBelow16 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(ldt);
        businessRepository.deleteAll();
        userRepository.deleteAll();

        testUser1 = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser2 = new User.Builder()
                .withFirstName("Dave")
                .withMiddleName("Joe")
                .withLastName("Bloggs")
                .withNickName("Dave")
                .withEmail("dave@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser3 = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("Davidson")
                .withLastName("Smith")
                .withNickName("Bobby")
                .withEmail("bobbysmith99@gmail.com")
                .withPassword("1440-H%nt3r2")
                .withBio("Likes slow walks on the beach")
                .withDob(ageBelow16)
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testUser3 = userRepository.save(testUser3);
        testBusiness1 = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(new Location())
                .withDescription("Some description")
                .withName("BusinessName")
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness1 = businessRepository.save(testBusiness1);

    }
    /**
     * Test that when a User is an admin of a business, the set of business admins contains that user
     * @throws Exception
     */
    @Test
    public void getAdministratorsReturnsAdministrators() throws Exception {
        testBusiness1.addAdmin(testUser2);
        testBusiness1 = businessRepository.save(testBusiness1);
        assertTrue(testBusiness1.getAdministrators().stream().anyMatch(user -> user.getUserID() == testUser2.getUserID()));
    }

    /**
     * Test that getting the primary owner correctly returns the primary owner
     * @throws Exception
     */
    @Test
    public void getPrimaryOwnerReturnsPrimaryOwner() throws Exception {
        assertEquals(testUser1.getUserID(), testBusiness1.getPrimaryOwner().getUserID());
    }

    /**
     * No errors should be thrown when the type is a valid business type
     * @throws ResponseStatusException
     */
    @Test
    public void setBusinessTypeWhenValid() throws ResponseStatusException {
        testBusiness1.setBusinessType("Retail Trade");
        assertEquals( "Retail Trade", testBusiness1.getBusinessType());
    }

    /**
     * Test that an error is thrown when the business type is invalid
     * @throws ResponseStatusException
     */
    @Test
    public void setBusinessTypeThrowsWhenInvalid() throws ResponseStatusException {
        try {
            testBusiness1.setBusinessType("invalid type");
            fail(); // shouldnt get here
        } catch (ResponseStatusException err) {

        }
    }

    /**
     * Test that setting the business name changes the business name
     */
    @Test
    public void setNameSetsName() {
        String newName = "Cool Business";
        testBusiness1.setName(newName);
        assertEquals(newName, testBusiness1.getName());
    }

    /**
     * test that setting the business description sets the business description
     */
    @Test
    public void setDescriptionSetsDescription() {
        String newDesc = "Some description";
        testBusiness1.setDescription(newDesc);
        assertEquals(newDesc, testBusiness1.getDescription());
    }

    /**
     * tests that if user is above or equal 16 years old, the user's data will be stored as the primary owner
     */
    @Test
    public void setAboveMinimumAge() {
        assertNotNull(testBusiness1.getPrimaryOwner());
    }

    /**
     * tests that if user is below 16 years old, an exception will be thrown
     */
    @Test
    public void setBelowMinimumAge() {
        Exception thrown = assertThrows(ResponseStatusException.class, () -> {
            Business testBusiness2 = new Business.Builder()
            .withBusinessType("Accommodation and Food Services")
            .withAddress(new Location())
            .withDescription("Some description")
            .withName("BusinessName")
            .withPrimaryOwner(testUser3)
            .build();
            testBusiness2 = businessRepository.save(testBusiness2);
        }, "Expected Business.builder() to throw, but it didn't" );

        System.out.print(thrown);
        assertTrue(thrown.getMessage().contains("User is not of minimum age required to create a business"));
    }

    /**
     * Check that when setName is called with a name which is 100 characters or less, contains only letters, numbers and
     * the characters "@ $ % & . , ' ; : - _", and is not empty, the businesses name is set to that value.
     */
    @Test
    public void setNameValidNameTest() {
        fail("Not yet implemented");
        String[] testNames = {"Joe's cake shop", "BNZ", "X", ""};
        for (String name : testNames) {
            testBusiness1.setName(name);
            assertEquals(testBusiness1.getName(), name);
        }
    }

    /**
     * Check that when setName is called on a business with a valid name and that business is saved to the repository,
     * the name associated with the saved entity is updated.
     */
    @Test
    public void setNameSavedToRepositoryTest() {fail("Not yet implemented");}

    /**
     * Check that when setName is called with a name which contains characters which are not letters, numbers or
     * the characters "@ $ % & . , ; : - _", a response status exception with status code 400 and message "The business
     * name can contain only letters, numbers, and the special characters @ $ % & - _ , . : ;" will be thrown and the
     * business's name will not be changed.
     */
    @Test
    public void setNameInvalidCharacterTest() {fail("Not yet implemented");}

    /**
     * Check that when setName is called with a name which is greater than 100 characters lonog, a response status
     * exception with status code 400 and message "The business name must be 100 characters or fewer" will be thrown
     * and the business's name will not be changed.
     */
    @Test
    public void setNameTooLongTest() {fail("Not yet implemented");}

    /**
     * Check that when setName is called with null as its argument, a response status expection will be thrown with
     * status code 400 and message "The business name must not be empty" and the business's name will not be changed.
     */
    @Test
    public void setNameNullTest() {fail("Not yet implemented");}

    /**
     * Check that when setName is called with the empty string as its argument, a response status expection will be thrown
     * with status code 400 and message "The business name must not be empty" and the business's name will not be changed.
     */
    @Test
    public void setNameEmptyStringTest() {fail("Not yet implemented");}

    /**
     * Check that when setName is called with a blank string as its argument, a response status expection will be thrown
     * with status code 400 and message "The business name must not be empty" and the business's name will not be changed.
     */
    @Test
    public void setNameBlankStringTest() {fail("Not yet implemented");}

    /**
     * Check that when setDescription is called with a string which is 100 characters or less, contains only letters,
     * and the characters "@ $ % & . , ; : - _", and is not empty, the businesses description is set to that value.
     */
    @Test
    public void setDescriptionValidDescriptionTest() {fail("Not yet implemented");}

    /**
     * Check that when setDescription is called on a business with a valid name and that business is saved to the repository,
     * the description associated with the saved entity is updated.
     */
    @Test
    public void setDescriptionSavedToRepositoryTest() {fail("Not yet implemented");}

    /**
     * Check that when setDescription is called with a name which contains characters which are not letters, numbers or
     * the characters "@ $ % & . , ; : - _", a response status exception with status code 400 and message "The business
     * description can contain only letters, numbers, and the special characters @ $ % & - _ , . : ;" will be thrown and the
     * business's description will not be changed.
     */
    @Test
    public void setDescriptionInvalidCharacterTest() {fail("Not yet implemented");}

    /**
     * Check that when setDescription is called with a string which is greater than 200 characters lonog, a response status
     * exception with status code 400 and message "The business description must be 100 characters or fewer" will be thrown
     * and the business's description will not be changed.
     */
    @Test
    public void setDescriptionTooLongTest() {fail("Not yet implemented");}

    /**
     * Check that when setDescription is called with null as its argument, a response status expection will be thrown
     * with status code 400 and message "The business description must not be empty" and the business's description will
     * not be changed.
     */
    @Test
    public void setDescriptionNullTest() {fail("Not yet implemented");}

    /**
     * Check that when setDescription is called with the empty string as its argument, a response status expection will
     * be thrown with status code 400 and message "The business description must not be empty" and the business's
     * description will not be changed.
     */
    @Test
    public void setDescriptionEmptyStringTest() {fail("Not yet implemented");}

    /**
     * Check that when setDescription is called with a blank string as its argument, a response status expection will be
     * thrown with status code 400 and message "The business description must not be empty" and the business's
     * description will not be changed.
     */
    @Test
    public void setDescriptionBlankStringTest() {fail("Not yet implemented");}

    /**
     * Check that when setPrimaryOwner is called with null as its argument, a response status exception with status code
     * 400 is thrown and the primary owner is not changed.
     */
    @Test
    public void setPrimaryOwnerNullTest() {fail("Not yet implemented");}

    /**
     * Check that if setPrimaryOwner is called with a user who has not been saved to the database, a response status
     * exception with status code 400 is thrown and the primary owner is not changed.
     */
    @Test
    public void setPrimaryOwnerUnsavedUserTest() {fail("Not yet implemented");}

    /**
     * Check that if someone attempts to remove a user who is currently the primary owner of a business from the database,
     * an expection is thrown and the user is not removed from the database.
     */
    @Test
    public void primaryOwnerCantBeDeletedTest() {fail("Not yet implemented");}

    /**
     * Check that if addAdmin is called with a user who is already an admin of the business, a response status expection
     * with status code 400 is thrown adn the admin is not added.
     */
    @Test
    public void addAdminCurrentAdminTest() {fail("Not yet implemented");}

    /**
     * Check that if addAdmin is called with a user who is already the primary owner of the business, a response status
     * expection with status code 400 is thrown and the admin is not added.
     */
    @Test
    public void addAdminPrimaryOwnerTest() {fail("Not yet implemented");}

    /**
     * Check that if addAdmin is called with a user who is not currently an admin of the business, that user is added to
     * the businesses list of administrators.
     */
    @Test
    public void addAdminNewAdminTest() {fail("Not yet implemented");}

    /**
     * Check that if a user who is currently an admin of a business is deleted from the database, that user will be
     * removed from the business's list of administrators.
     */
    @Test
    public void adminDeletedTest() {fail("Not yet implemented");}

    /**
     * Test that when setCreated is called, the business's created attribute will be set to the current date and time
     */
    @Test
    public void setCreatedTest() {fail("Not yet implemented");}

}
