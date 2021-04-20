package org.seng302.Entities;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        assertEquals("Retail Trade", testBusiness1.getBusinessType());
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
        }, "Expected Business.builder() to throw, but it didn't");

        assertTrue(thrown.getMessage().contains("User is not of minimum age required to create a business"));
    }

    /**
     * Helper function that returns an array of valid names to use in tests.
     *
     * @return An array of valid business names various lengths and character types
     */
    private String[] getTestNames() {
        StringBuilder longBusinessNameBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longBusinessNameBuilder.append("a");
        }
        return new String[]{"Joe's cake shop", "BNZ", "X", "cool business", "big-business", "$%@&.,:;", "BUSINESS123", "" +
                "another_business", longBusinessNameBuilder.toString()};
    }


    /**
     * Check that when setName is called with a name which is 100 characters or less, contains only letters, numbers and
     * the characters "@ $ % & . , ' ; : - _", and is not empty, the businesses name is set to that value.
     */
    @Test
    public void setNameValidNameTest() {
        String[] testNames = getTestNames();
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
    public void setNameSavedToRepositoryTest() {
        String[] testNames = getTestNames();
        for (String name : testNames) {
            testBusiness1.setName(name);
            businessRepository.save(testBusiness1);
            testBusiness1 = businessRepository.findById(testBusiness1.getId()).get();
            assertEquals(name, testBusiness1.getName());
        }
    }

    /**
     * Check that when setName is called with a name which contains characters which are not letters, numbers or
     * the characters "@ $ % & . , ; : - _", a response status exception with status code 400 will be thrown and the
     * business's name will not be changed.
     */
    @Test
    public void setNameInvalidCharacterTest() {
        String originalName = testBusiness1.getName();
        String[] invalidCharacterNames = {"?", "^^^^^^^", "business*", "!This is not allowed", "(or this)"};
        for (String name : invalidCharacterNames) {
            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
                testBusiness1.setName(name);
            });
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals(originalName, testBusiness1.getName());
        }
    }

    /**
     * Check that when setName is called with a name which is greater than 100 characters lonog, a response status
     * exception with status code 400 will be thrown and the business's name will not be changed.
     */
    @Test
    public void setNameTooLongTest() {
        String originalName = testBusiness1.getName();

        StringBuilder justOver = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            justOver.append("x");
        }
        StringBuilder wayTooLong = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            wayTooLong.append("y");
        }
        String[] longNames = {justOver.toString(), wayTooLong.toString()};

        for (String name : longNames) {
            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
                testBusiness1.setName(name);
            });
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals(originalName, testBusiness1.getName());
        }
    }

    /**
     * Check that when setName is called with null as its argument, a response status expection will be thrown with
     * status code 400 and the business's name will not be changed.
     */
    @Test
    public void setNameNullTest() {
        String originalName = testBusiness1.getName();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setName(null);
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalName, testBusiness1.getName());
    }

    /**
     * Check that when setName is called with the empty string as its argument, a response status expection will be thrown
     * with status code 400 and the business's name will not be changed.
     */
    @Test
    public void setNameEmptyStringTest() {
        String originalName = testBusiness1.getName();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setName("");
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalName, testBusiness1.getName());
    }

    /**
     * Check that when setName is called with a blank string as its argument, a response status expection will be thrown
     * with status code 400 and the business's name will not be changed.
     */
    @Test
    public void setNameBlankStringTest() {
        String originalName = testBusiness1.getName();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setName("      ");
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalName, testBusiness1.getName());
    }

    /**
     * Helper function that returns an array of valid descriptions to use in tests. Includes all the same strings as
     * returned by getTestNames plus a 200 character string to test that long descriptions are accepted.
     * @return An array of valid business names various lengths and character types
     */
    private String[] getTestDescriptions() {
        String[] testNames = getTestNames();
        String[] testDescriptions = new String[testNames.length + 1];
        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longDescription.append("f");
        }
        for (int i = 0; i < testNames.length; i++) {
            testDescriptions[i] = testNames[i];
        }
        testDescriptions[testDescriptions.length - 1] = longDescription.toString();
        return testDescriptions;
    }

    /**
     * Check that when setDescription is called with a string which is 200 characters or less, contains only letters,
     * and the characters "@ $ % & . , ; : - _", and is not empty, the businesses description is set to that value.
     */
    @Test
    public void setDescriptionValidDescriptionTest() {
        String[] testDescriptions = getTestDescriptions();
        for (String description : testDescriptions) {
            testBusiness1.setDescription(description);
            assertEquals(description, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called on a business with a valid name and that business is saved to the repository,
     * the description associated with the saved entity is updated.
     */
    @Test
    public void setDescriptionSavedToRepositoryTest() {
        String[] testDescriptions = getTestDescriptions();
        for (String description : testDescriptions) {
            testBusiness1.setDescription(description);
            businessRepository.save(testBusiness1);
            testBusiness1 = businessRepository.findById(testBusiness1.getId()).get();
            assertEquals(description, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called with a name which contains characters which are not letters, numbers or
     * the characters "@ $ % & . , ; : - _", a response status exception with status code 400 will be thrown and the
     * business's description will not be changed.
     */
    @Test
    public void setDescriptionInvalidCharacterTest() {
        String originalDescription = testBusiness1.getDescription();
        String[] invalidCharacterDescriptions = {"?", "^^^^^^^", "business*", "!This is not allowed", "(or this)"};
        for (String description : invalidCharacterDescriptions) {
            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
                testBusiness1.setDescription(description);
            });
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals(originalDescription, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called with a string which is greater than 200 characters lonog, a response status
     * exception with status code 400 will be thrown and the business's description will not be changed.
     */
    @Test
    public void setDescriptionTooLongTest() {
        String originalDescription = testBusiness1.getDescription();

        StringBuilder justOver = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            justOver.append("x");
        }
        StringBuilder wayTooLong = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            wayTooLong.append("y");
        }
        String[] longDescriptions = {justOver.toString(), wayTooLong.toString()};

        for (String description : longDescriptions) {
            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
                testBusiness1.setDescription(description);
            });
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
            assertEquals(originalDescription, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called with null as its argument, a response status expection will be thrown
     * with status code 400 and the business's description will not be changed.
     */
    @Test
    public void setDescriptionNullTest() {
        String originalDescription = testBusiness1.getDescription();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setDescription(null);
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalDescription, testBusiness1.getDescription());
    }

    /**
     * Check that when setDescription is called with the empty string as its argument, a response status expection will
     * be thrown with status code 400 and the business's description will not be changed.
     */
    @Test
    public void setDescriptionEmptyStringTest() {
        String originalDescription = testBusiness1.getDescription();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setDescription("");
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalDescription, testBusiness1.getDescription());
    }

    /**
     * Check that when setDescription is called with a blank string as its argument, a response status expection will be
     * thrown with status code 400 and the business's description will not be changed.
     */
    @Test
    public void setDescriptionBlankStringTest() {
        String originalDescription = testBusiness1.getDescription();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setDescription("          ");
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalDescription, testBusiness1.getDescription());
    }

    /**
     * Check that if someone attempts to remove a user who is currently the primary owner of a business from the database,
     * an expection is thrown and the user is not removed from the database.
     */
    @Test
    public void primaryOwnerCantBeDeletedTest() {
        User primaryOwner = testBusiness1.getPrimaryOwner();
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.deleteById(primaryOwner.getUserID());
        });
        assertNotNull(userRepository.findById(primaryOwner.getUserID()).get());
    }

    /**
     * Check that if addAdmin is called with a user who is already an admin of the business, a response status expection
     * with status code 400 is thrown adn the admin is not added.
     */
    @Test
    public void addAdminCurrentAdminTest() {
        testBusiness1.addAdmin(testUser2);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.addAdmin(testUser2);
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(1, testBusiness1.getAdministrators().size());
        assertTrue(testBusiness1.getAdministrators().contains(testUser2));
    }

    /**
     * Check that if addAdmin is called with a user who is already the primary owner of the business, a response status
     * expection with status code 400 is thrown and the admin is not added.
     */
    @Test
    public void addAdminPrimaryOwnerTest() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.addAdmin(testUser1);
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(0, testBusiness1.getAdministrators().size());
    }

    /**
     * Check that if addAdmin is called with a user who is not currently an admin of the business, that user is added to
     * the businesses list of administrators.
     */
    @Test
    public void addAdminNewAdminTest() {
        testBusiness1.addAdmin(testUser2);
        assertEquals(1, testBusiness1.getAdministrators().size());
        assertTrue(testBusiness1.getAdministrators().contains(testUser2));
    }

    /**
     * Check that if someone attempts to delete a user who is currently an admin of a business from the database,
     * a DataIntegrityVioloationException will be thrown, and the user and business will not be changed.
     */
    @Test
    public void adminDeletedTest() {
        testBusiness1.addAdmin(testUser2);
        testBusiness1 = businessRepository.save(testBusiness1);
        long adminId = testUser2.getUserID();
        long businessId = testBusiness1.getId();
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.deleteById(testUser2.getUserID());
        });
        assertTrue(userRepository.existsById(adminId));
        assertTrue(businessRepository.existsById(businessId));
        testBusiness1 = businessRepository.findById(businessId).get();
        assertEquals(1, testBusiness1.getAdministrators().size());
        for (User user : testBusiness1.getAdministrators()) {
            assertEquals(adminId, user.getUserID());
        }
    }

    /**
     * Test that when a business which has not admins is deleted, that business is removed from the repository
     * but its primary owner entity is not deleted.
     */
    @Test
    public void deleteBusinessWithoutAdminsTest() {
        long businessId = testBusiness1.getId();
        long ownerId = testBusiness1.getPrimaryOwner().getUserID();
        businessRepository.deleteById(businessId);
        assertFalse(businessRepository.existsById(businessId));
        assertTrue(userRepository.existsById(ownerId));
        assertTrue(userRepository.findById(ownerId).get().getBusinessesOwned().isEmpty());
    }

    /**
     * Test that when delete is called on a business which has administrators, a DataIntegrityViolationException
     * is thrown and the business and its admins remain in the database.
     */
    public void deleteBusinessWithAdminsTest() {
        testBusiness1.addAdmin(testUser2);
        testBusiness1 = businessRepository.save(testBusiness1);
        long adminId = testUser2.getUserID();
        long businessId = testBusiness1.getId();
        assertThrows(DataIntegrityViolationException.class, () -> {
            businessRepository.deleteById(businessId);
        });
        assertTrue(userRepository.existsById(adminId));
        assertTrue(businessRepository.existsById(businessId));
        testBusiness1 = businessRepository.findById(businessId).get();
        assertEquals(1, testBusiness1.getAdministrators().size());
        for (User user : testBusiness1.getAdministrators()) {
            assertEquals(adminId, user.getUserID());
        }
    }

    /**
     * Test that when setCreated is called, and the business's created attribute is null,
     * the business's created attribute will be set to the current date and time
     */
    @Test
    public void setCreatedInitialValueTest() {
        Date now = new Date();
        Business testBusiness2 = new Business.Builder().withBusinessType("Non-profit organisation").withName("Zesty Business")
                .withAddress(Location.covertAddressStringToLocation("101,My Street,Christchurch,Canterbury,New Zealand,1010"))
                .withDescription("A nice place").withPrimaryOwner(testUser2).build();
        // Check that the difference between the time the business was created and the time at the start of exection of
        // this function is less than 1 second
        assertTrue(testBusiness2.getCreated().getTime() - now.getTime() < 1000);
    }

    /**
     * Test that when setAddress is called with null as the address, a response status exception with status code 400
     * will be thrown and the business's address will not be changed.
     */
    @Test
    public void setAddressNullTest() {
        Location originalAddress = testBusiness1.getAddress();
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.setAddress(null);
        });
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals(originalAddress, testBusiness1.getAddress());
    }

    /**
     * Test than when setAddress is called with a Location object as the argument, the business's address will be set to
     * the given location.
     */
    @Test
    public void setAddressValidTest() {
        Location address = Location.covertAddressStringToLocation("44,Humbug Ave,Hamilton,Waikato,New Zealand,1000");
        testBusiness1.setAddress(address);
        assertEquals(address, testBusiness1.getAddress());
    }


    /**
     * Helper function for constructJson tests. Creates a list containing JSONObject returned by
     * constructJson method when called on the given business with true, false or no arguement.
     * @return A lsit of JSONObjects produced by calling constructJson with true, false and no arg
     */
    private List<JSONObject> getTestJsons(Business business) {
        List<JSONObject> testJsons = new ArrayList<>();
        testJsons.add(business.constructJson(true));
        testJsons.add(business.constructJson(false));
        testJsons.add(business.constructJson());
        return testJsons;
    }

    /**
     * Test that the JSONObject returned by constructJson contains the fields id, primaryAdministratorId,
     * name, description, businessType, created, administrators and address when constructJson is
     * called with true as its arguement.
     */
    @Test
    public void constructJsonHasExpectedFieldsFullDetailsTrueTest() {
       JSONObject json = testBusiness1.constructJson(true);
       assertTrue(json.containsKey("name"));
       assertTrue(json.containsKey("description"));
       assertTrue(json.containsKey("businessType"));
       assertTrue(json.containsKey("address"));
       assertTrue(json.containsKey("id"));
       assertTrue(json.containsKey("primaryAdministratorId"));
       assertTrue(json.containsKey("administrators"));
       assertTrue(json.containsKey("created"));
    }

    /**
     * Test that the JSONObject returned by constructJson contains the fields id, primaryAdministratorId,
     * name, description, businessType, created and address when constructJson is
     * called with false or no argument.
     */
    @Test
    public void constructJsonHasExpectedFieldsFullDetailsFalseTest() {
        List<JSONObject> testJsons = new ArrayList<>();
        testJsons.add(testBusiness1.constructJson(false));
        testJsons.add(testBusiness1.constructJson());
        for (JSONObject json : testJsons) {
            assertTrue(json.containsKey("name"));
            assertTrue(json.containsKey("description"));
            assertTrue(json.containsKey("businessType"));
            assertTrue(json.containsKey("address"));
            assertTrue(json.containsKey("id"));
            assertTrue(json.containsKey("primaryAdministratorId"));
            assertTrue(json.containsKey("created"));
        }
    }

    /**
     * Test that the JSONObject returned by contructJson does not contain any fields other than
     * id, primaryAdministratorId, name, description, businessType, created, administraters and address,
     * whether constructJson is called with true as its argument.
     */
    @Test
    public void constructJsonDoesntHaveUnexpectedFieldsFullDetailsTrueTest() {
        JSONObject json = testBusiness1.constructJson(true);
        json.remove("name");
        json.remove("description");
        json.remove("businessType");
        json.remove("address");
        json.remove("id");
        json.remove("primaryAdministratorId");
        json.remove("administrators");
        json.remove("created");
        assertTrue(json.isEmpty());
    }

    /**
     * Test that the JSONObject returned by contructJson does not contain any fields other than
     * id, primaryAdministratorId, name, description, businessType, created and address,
     * whether constructJson is called with false or no argument.
     */
    @Test
    public void constructJsonDoesntHaveUnexpectedFieldsFullDetailsFalseTest() {
        List<JSONObject> testJsons = new ArrayList<>();
        testJsons.add(testBusiness1.constructJson(false));
        testJsons.add(testBusiness1.constructJson());
        for (JSONObject json : testJsons) {
            json.remove("name");
            json.remove("description");
            json.remove("businessType");
            json.remove("address");
            json.remove("id");
            json.remove("primaryAdministratorId");
            json.remove("created");
            assertTrue(json.isEmpty());
        }
    }

    /**
     * Test that id, primaryAdministratorId, name, description, businessType, created, and address
     * in the JSONObject returned by constructJson have the expecte value, whether cosntructJson is
     * called with true, false or no argument.
     */
    @Test
    public void constructJsonSimpleFieldsHaveExpectedValueTest() {
        List<JSONObject> testJsons = getTestJsons(testBusiness1);
        for (JSONObject json : testJsons) {
            assertEquals(testBusiness1.getName(), json.getAsString("name"));
            assertEquals(testBusiness1.getDescription(), json.getAsString("description"));
            assertEquals(testBusiness1.getBusinessType(), json.getAsString("businessType"));
            assertEquals(testBusiness1.getAddress().constructFullJson().toString(), json.getAsString("address"));
            assertEquals(testBusiness1.getId().toString(), json.getAsString("id"));
            assertEquals(testBusiness1.getPrimaryOwner().getUserID().toString(), json.getAsString("primaryAdministratorId"));
            assertEquals(testBusiness1.getCreated().toString(), json.getAsString("created"));
        }
    }

    /**
     * Test that when constructJson is called with true as its argument, the administrators field
     * contains a list of User JSONs with the details of the business's administrators.
     */
    @Test
    public void constructJsonAdministratorsFullDetailsTest() {
        testBusiness1.addAdmin(testUser2);
        assertEquals(2, testBusiness1.getOwnerAndAdministrators().size());
        List<User> admins = new ArrayList<>();
        admins.addAll(testBusiness1.getOwnerAndAdministrators());
        Collections.sort(admins, (User user1, User user2) ->
           user1.getUserID().compareTo(user2.getUserID()));
        JSONArray expectedAdminArray = new JSONArray();
        for (User user : admins) {
            expectedAdminArray.add(user.constructPublicJson());
        }
        String expectedAdminString = expectedAdminArray.toJSONString();
        JSONObject testJson = testBusiness1.constructJson(true);
        assertEquals(expectedAdminString, testJson.getAsString("administrators"));
    }

    /**
     * Test that when the business has a primary owner but not administrators, the getOwnerAndAdministrators
     * method will return a set containing just the business's primary owner.
     */
    @Test
    public void getOwnerAndAdministratorsNoAdministratorsTest() {
        assertEquals(1, testBusiness1.getOwnerAndAdministrators().size());
        assertTrue(testBusiness1.getOwnerAndAdministrators().contains(testUser1));
    }

    /**
     * Test that when the business has administrators added in addition to its primary owner, the
     * getOwnerAndAdministrators method will return a set containing the owner and all administrators
     * of the business.
     */
    @Test
    public void getOwnerAndAdministratorsAdminsAddedTest() {
        testBusiness1.addAdmin(testUser2);
        assertEquals(2, testBusiness1.getOwnerAndAdministrators().size());
        assertTrue(testBusiness1.getOwnerAndAdministrators().contains(testUser1));
        assertTrue(testBusiness1.getOwnerAndAdministrators().contains(testUser2));
    }



}
