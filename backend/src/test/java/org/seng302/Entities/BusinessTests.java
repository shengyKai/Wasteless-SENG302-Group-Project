package org.seng302.Entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;


import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
     * Helper function for constructJson tests. Creates a list containing JSONObject returned by
     * constructJson method when called on the given business with true, false or no arguement.
     * @return A lsit of JSONObjects produced by calling constructJson with true, false and no arg
     */
    private List<JSONObject> getTestJsons(Business business) {
        List<JSONObject> testJsons = new ArrayList<>();
        testJsons.add(testBusiness.constructJson(true));
        testJsons.add(testBusiness.constructJson(false));
        testJsons.add(testBusiness.constructJson());
        return testJsons;
    }

    /**
     * Test that the JSONObject returned by constructJson contains the fields id, primaryAdministratorId,
     * name, description, businessType, created, administraters and address whether constructJson is
     * called with true, false or no argument.
     */
    @Test
    public void constructJsonHasExpectedFieldsTest() {
       List<JSONObject> testJsons = getTestJsons(testBusiness);
       for (JSONObject json : testJsons) {
           assertTrue(json.containsKey("name"));
           assertTrue(json.containsKey("description"));
           assertTrue(json.containsKey("businessType"));
           assertTrue(json.containsKey("address"));
           assertTrue(json.containsKey("id"));
           assertTrue(json.containsKey("primaryAdministratorId"));
           assertTrue(json.containsKey("administrators"));
           assertTrue(json.containsKey("created"));
       }
    }

    /**
     * Test that the JSONObject returned by contructJson does not contain any fields other than
     * id, primaryAdministratorId, name, description, businessType, created, administraters and address,
     * whether constructJson is called with true, false or no argument.
     */
    @Test
    public void constructJsonDoesntHaveUnexpectedFieldsTest() {
        List<JSONObject> testJsons = getTestJsons(testBusiness);
        for (JSONObject json : testJsons) {
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
    }

    /**
     * Test that id, primaryAdministratorId, name, description, businessType, created, and address
     * in the JSONObject returned by constructJson have the expecte value, whether cosntructJson is
     * called with true, false or no argument.
     */
    @Test
    public void constructJsonSimpleFieldsHaveExpectedValueTest() {
        List<JSONObject> testJsons = getTestJsons(testBusiness);
        for (JSONObject json : testJsons) {
            assertEquals(testBusiness.getName(), json.getAsString("name"));
            assertEquals(testBusiness.getDescription(), json.getAsString("description"));
            assertEquals(testBusiness.getBusinessType(), json.getAsString("businessType"));
            assertEquals(testBusiness.getAddress().constructFullJson().toString(), json.getAsString("address"));
            assertEquals(testBusiness.getId().toString(), json.getAsString("id"));
            assertEquals(testBusiness.getPrimaryOwner().getUserID().toString(), json.getAsString("primaryAdministratorId"));
            assertEquals(testBusiness.getCreated().toString(), json.getAsString("created"));
        }
    }

    /**
     * Test that when constructJson is called with true as its argument, the administrators field
     * contains a list of User JSONs with the details of the business's administrators.
     */
    @Test
    public void constructJsonAdministratorsFullDetailsTest() {
        testBusiness.addAdmin(testUser2);
        assertEquals(2, testBusiness.getOwnerAndAdministrators().size());
        List<User> admins = new ArrayList<>();
        admins.addAll(testBusiness.getOwnerAndAdministrators());
        Collections.sort(admins, (User user1, User user2) ->
           user1.getUserID().compareTo(user2.getUserID()));
        JSONArray expectedAdminArray = new JSONArray();
        for (User user : admins) {
            expectedAdminArray.add(user.constructPublicJson());
        }
        String expectedAdminString = expectedAdminArray.toJSONString();
        JSONObject testJson = testBusiness.constructJson(true);
        assertEquals(expectedAdminString, testJson.getAsString("administrators"));
    }

    /**
     * Test that when constructJson is called with false as its argument or with no argument, the
     * administrators field constains the placeholder array ["string"].
     */
    @Test
    public void constructJsonAdministratorsPlaceholderArrayTest() {
        testBusiness.addAdmin(testUser2);
        assertEquals(2, testBusiness.getOwnerAndAdministrators().size());
        JSONObject falseJson = testBusiness.constructJson(false);
        assertArrayEquals(new String[] {"string"}, (String[]) falseJson.get("administrators"));
        JSONObject noArgJson = testBusiness.constructJson();
        assertArrayEquals(new String[] {"string"}, (String[]) noArgJson.get("administrators"));
    }

    /**
     * Test that when the business has a primary owner but not administrators, the getOwnerAndAdministrators
     * method will return a set containing just the business's primary owner.
     */
    @Test
    public void getOwnerAndAdministratorsNoAdministratorsTest() {
        assertEquals(1, testBusiness.getOwnerAndAdministrators().size());
        assertTrue(testBusiness.getOwnerAndAdministrators().contains(testUser1));
    }

    /**
     * Test that when the business has administrators added in addition to its primary owner, the
     * getOwnerAndAdministrators method will return a set containing the owner and all administrators
     * of the business.
     */
    @Test
    public void getOwnerAndAdministratorsAdminsAddedTest() {
        testBusiness.addAdmin(testUser2);
        assertEquals(2, testBusiness.getOwnerAndAdministrators().size());
        assertTrue(testBusiness.getOwnerAndAdministrators().contains(testUser1));
        assertTrue(testBusiness.getOwnerAndAdministrators().contains(testUser2));
    }



}
