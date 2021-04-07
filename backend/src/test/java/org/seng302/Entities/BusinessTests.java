package org.seng302.Entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BusinessTests {

    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    UserRepository userRepository;

    User testUser1;
    User testUser2;
    Business testBusiness;

    /**
     * Sets up 2 Users and 1 Business
     * User1 is primary owner of the business
     * @throws ParseException
     */
    @BeforeEach
    public void setUp() throws ParseException {
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
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress("4 Rountree Street, Upper Riccarton")
                .build();
        testUser2 = new User.Builder()
                .withFirstName("Dave")
                .withMiddleName("Joe")
                .withLastName("Bloggs")
                .withNickName("Dave")
                .withEmail("dave@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress("4 Rountree Street, Upper Riccarton")
                .build();
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(new Location())
                .withDescription("Some description")
                .withName("BusinessName")
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness = businessRepository.save(testBusiness);

    }
    /**
     * Test that when a User is an admin of a business, the set of business admins contains that user
     * @throws Exception
     */
    @Test
    public void getAdministratorsReturnsAdministrators() throws Exception {
        testBusiness.addAdmin(testUser2);
        testBusiness = businessRepository.save(testBusiness);
        assertTrue(testBusiness.getAdministrators().stream().anyMatch(user -> user.getUserID() == testUser2.getUserID()));
    }

    /**
     * Test that getting the primary owner correctly returns the primary owner
     * @throws Exception
     */
    @Test
    public void getPrimaryOwnerReturnsPrimaryOwner() throws Exception {
        assertEquals(testUser1.getUserID(), testBusiness.getPrimaryOwner().getUserID());
    }

    /**
     * No errors should be thrown when the type is a valid business type
     * @throws ResponseStatusException
     */
    @Test
    public void setBusinessTypeWhenValid() throws ResponseStatusException {
        testBusiness.setBusinessType("Retail Trade");
        assertEquals( "Retail Trade", testBusiness.getBusinessType());
    }

    /**
     * Test that an error is thrown when the business type is invalid
     * @throws ResponseStatusException
     */
    @Test
    public void setBusinessTypeThrowsWhenInvalid() throws ResponseStatusException {
        try {
            testBusiness.setBusinessType("invalid type");
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
        testBusiness.setName(newName);
        assertEquals(newName, testBusiness.getName());
    }

    /**
     * test that setting the business description sets the business description
     */
    @Test
    public void setDescriptionSetsDescription() {
        String newDesc = "Some description";
        testBusiness.setDescription(newDesc);
        assertEquals(newDesc, testBusiness.getDescription());
    }
}
