package org.seng302.Controllers;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.seng302.Entities.Business;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BusinessControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserRepository userRepository;

    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();
    private Cookie authCookie;
    private Business testBusiness;
    private User owner;


    /**
     * Add a user object to the userRepository and construct an authorization token to be used for this session.
     *
     * @throws ParseException
     */
    @BeforeEach
    public void setUp() throws ParseException, IOException {
        setUpAuthCode();
        setUpTestUser();
        setUpTestBusiness();
    }

    private void setCurrentUser(Long userId) {
        sessionAuthToken.put("accountId", userId);
    }

    /**
     * This method creates an authentication code for sessions and cookies.
     */
    private void setUpAuthCode() {
        StringBuilder authCodeBuilder = new StringBuilder();
        authCodeBuilder.append("0".repeat(64));
        String authCode = authCodeBuilder.toString();
        sessionAuthToken.put("AUTHTOKEN", authCode);
        authCookie = new Cookie("AUTHTOKEN", authCode);
    }
    /**
     * This method creates a user and adds it to the repository.
     *
     * @throws ParseException
     */
    private void setUpTestBusiness() throws ParseException {
        testBusiness = new Business.Builder()
                .withBusinessType("Accommodation and Food Services")
                .withAddress(new Location())
                .withDescription("Some description")
                .withName("COSC co")
                .withPrimaryOwner(owner)
                .build();
        businessRepository.save(testBusiness);
    }
    /**
     * This method creates a user and adds it to the repository.
     *
     * @throws ParseException
     */
    private void setUpTestUser() throws ParseException {
        Location userAddress = new Location.Builder()
                .inCountry("New Zealand")
                .inRegion("Canterbury")
                .inCity("Christchurch")
                .inSuburb("Ilam")
                .atStreetNumber("123")
                .onStreet("Ilam road")
                .withPostCode("8041")
                .build();
        owner = new User.Builder()
                .withFirstName("John")
                .withMiddleName("Hector")
                .withLastName("Smith")
                .withNickName("Jonny")
                .withEmail("johnsmith99@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("Likes long walks on the beach")
                .withDob("2000-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(userAddress)
                .build();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(owner);
    }

    /**
     * AssertEquals each property of a Business as type JSON to type Object
     * If the two objects are equal, no error is thrown
     * @param json The JSON representation of a business
     * @param object The Object representation of a business
     */
    private void assertEquivalentJsonToObject(JSONObject json, Business object) {
        assertEquals(json.getAsString("primaryAdministratorId"), object.getPrimaryOwner().getUserID().toString());
        assertEquals(json.getAsString("name"), object.getName());
        assertEquals(json.getAsString("description"), object.getDescription());
        assertEquals(json.getAsString("businessType"), object.getBusinessType());
        JSONObject address = new JSONObject((Map<String, ?>) json.get("address"));
        assertEquals(address.getAsString("streetNumber"), object.getAddress().getStreetNumber().toString());
        assertEquals(address.getAsString("streetName"), object.getAddress().getStreetName());
        assertEquals(address.getAsString("city"), object.getAddress().getCity());
        assertEquals(address.getAsString("region"), object.getAddress().getRegion());
        assertEquals(address.getAsString("country"), object.getAddress().getCountry());
        assertEquals(address.getAsString("postcode"), object.getAddress().getPostCode());
    }

    /**
     * Test for registering a business in a blue sky scenario
     * Session logged in as the given primaryAdministratorId
     * All request values are valid
     * @throws Exception
     */
    @Test
    public void RegisterBusinessTest() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"city\": \"Christchurch\",\n" +
                        "    \"region\": \"Canterbury\",\n" +
                        "    \"country\": \"New Zealand\",\n" +
                        "    \"postcode\": \"90210\"\n" +
                        "  },\n" +
                        "  \"businessType\": \"Accommodation and Food Services\"\n" +
                        "}", owner.getUserID());
        setCurrentUser(owner.getUserID());
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses")
                .content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Business newBusiness = businessRepository.findByName("Lumbridge General Store");
        assertEquivalentJsonToObject(businessJson, newBusiness);
    }

    /**
     * Test for registering a business when the business type is not one of the expected types
     * Session logged in as the given primaryAdministratorId
     * Business type value is invalid
     * @throws Exception
     */
    @Test
    public void RegisterBusinessInvalidBusinessTypeTest() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"city\": \"Christchurch\",\n" +
                        "    \"region\": \"Canterbury\",\n" +
                        "    \"country\": \"New Zealand\",\n" +
                        "    \"postcode\": \"90210\"\n" +
                        "  },\n" +
                        "  \"businessType\": \"An invalid BUSINESS TYPE\"\n" +
                        "}", owner.getUserID());
        setCurrentUser(owner.getUserID());
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses")
                .content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test for registering a business when not logged in as a user
     * Session not logged in
     * All request values are valid
     * @throws Exception
     */
    @Test
    public void RegisterBusinessWhenNotLoggedIn() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"city\": \"Christchurch\",\n" +
                        "    \"region\": \"Canterbury\",\n" +
                        "    \"country\": \"New Zealand\",\n" +
                        "    \"postcode\": \"90210\"\n" +
                        "  },\n" +
                        "  \"businessType\": \"Accommodation and Food Services\"\n" +
                        "}", owner.getUserID());
        setCurrentUser(owner.getUserID());
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses")
                .content(businessJson.toJSONString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test for registering a business when the given primaryAdministrator doesn't exist
     * Session logged in as the given primaryAdministratorId
     * primaryAdministrator is invalid
     * @throws Exception
     */
    @Test
    public void RegisterBusinessInvalidIdTest() throws Exception {
        String businessJsonString =
                "{\n" +
                        "  \"primaryAdministratorId\": 999,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"city\": \"Christchurch\",\n" +
                        "    \"region\": \"Canterbury\",\n" +
                        "    \"country\": \"New Zealand\",\n" +
                        "    \"postcode\": \"90210\"\n" +
                        "  },\n" +
                        "  \"businessType\": \"Accommodation and Food Services\"\n" +
                        "}";
        setCurrentUser(owner.getUserID());
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses")
                .content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test for registering a business under someone else's name
     * Session logged in as a different user than the provided primaryAdministratorId
     * primaryAdministrator is invalid
     * @throws Exception
     */
    @Test
    public void RegisterBusinessNoPermissionTest() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"city\": \"Christchurch\",\n" +
                        "    \"region\": \"Canterbury\",\n" +
                        "    \"country\": \"New Zealand\",\n" +
                        "    \"postcode\": \"90210\"\n" +
                        "  },\n" +
                        "  \"businessType\": \"Accommodation and Food Services\"\n" +
                        "}", owner.getUserID());
        setCurrentUser(999L);
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/businesses")
                .content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    /**
     * Assert that when the owner of a business makes another user an admin,
     * the business is contained in the set of businessesAdministered for that user.
     * Assumes BusinessOwner is logged in performing the action
     * Assumes the given user is not already an admin
     * @throws Exception
     */
    @Test
    public void addAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(new Location())
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        testAdmin = userRepository.findById(testAdmin.getUserID()).get();
        assertTrue(testAdmin.getBusinessesAdministered().contains(testBusiness));
    }

    /**
     * Assert that when attempting to promote a user to admin when not currently logged in
     * as the businesses' primary Owner, a 403 is returned
     * @throws Exception
     */
    @Test
    public void addAdminWhenNotPrimaryOwnerTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(new Location())
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());

        setCurrentUser(999L); // LOGGED IN AS SOMEONE WHO IS NOT BUSINESS OWNER

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Assert that when attempting to promote a user to admin when not logged in at all,
     * a 401 is returned
     * @throws Exception
     */
    @Test
    public void addAdminWhenNotLoggedInTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(new Location())
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Assert that when promoting a user to admin with a userId that does not exist,
     * a 400 is returned
     * @throws Exception
     */
    @Test
    public void addAdminWhenUserNotExistTest() throws Exception {

        String jsonString = "{\"userId\": 99}";
        setCurrentUser(owner.getUserID());

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Assert that when promoting a user to admin when the business does not exist,
     * a 406 is returned
     * @throws Exception
     */
    @Test
    public void addAdminWhenBusinessNotExistTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(new Location())
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/businesses/%d/makeAdministrator", 999L))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());

    }

    /**
     * Assert that when promoting a user to admin, who is already an admin of this business,
     * a 400 is returned
     * @throws Exception
     */
    @Test
    public void addAdminWhenUserAlreadyAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(new Location())
                .build();
        testAdmin = userRepository.save(testAdmin);
        testBusiness.addAdmin(testAdmin);
        testBusiness = businessRepository.save(testBusiness);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * Assert that when removing a user's admin rights from a business, the user is no longer admin
     * Assumes logged in as business Primary Owner
     * Assumes given user is an admin
     * @throws Exception
     */
    @Test
    public void removeAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(new Location())
                .build();
        testAdmin = userRepository.save(testAdmin);

        testBusiness.addAdmin(testAdmin); // make user an admin
        testBusiness = businessRepository.save(testBusiness);

        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/businesses/%d/removeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        testAdmin = userRepository.findById(testAdmin.getUserID()).get();
        assertFalse(testAdmin.getBusinessesAdministered().contains(testBusiness));
    }

    /**
     * Assert that when removing a user's admin role from a business when they aren't an admin of,
     * a 400 is returned
     * @throws Exception
     */
    @Test
    public void removeAdminWhenUserNotAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2021-03-11")
                .withPhoneNumber("+64 3 555 0129")
                .withAddress(new Location())
                .build();
        testAdmin = userRepository.save(testAdmin);

        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/businesses/%d/removeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }



}
