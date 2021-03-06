package org.seng302.leftovers.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.seng302.leftovers.dto.business.BusinessResponseDTO;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.dto.user.UserRole;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class BusinessControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserRepository userRepository;

    @Resource
    private SessionFactory sessionFactory;

    @Captor
    private ArgumentCaptor<String> queryCaptor;
    @Captor
    private ArgumentCaptor<String> typeCaptor;

    @Autowired
    private ObjectMapper objectMapper;

    private final HashMap<String, Object> sessionAuthToken = new HashMap<>();
    private Cookie authCookie;
    private Business testBusiness;
    private User owner;
    private User admin;
    private User otherUser;
    private Session session;

    /**
     * Add a user object to the userRepository and construct an authorization token
     * to be used for this session.
     *
     * @throws ParseException
     */
    @BeforeEach
    void setUp() throws ParseException, IOException {
        businessRepository.deleteAll();
        userRepository.deleteAll();
        setUpAuthCode();
        setUpTestUser();
        setUpTestBusinesses();

        session = sessionFactory.openSession();
    }

    @AfterEach
    void tearDown() {
        businessRepository.deleteAll();
        userRepository.deleteAll();
        session.close();
    }

    /**
     * Mocks a session logged in as a given user
     * @param userId Log in with userId
     */
    private void setCurrentUser(Long userId) {
        sessionAuthToken.put("accountId", userId);
    }

    /**
     * Mocks logging in as a DGAA role
     */
    private void loginAsDgaa() {
        sessionAuthToken.put("role", UserRole.DGAA);
    }

    /**
     * Mocks logging in as a global application administrator role
     */
    private void loginAsGlobalAdmin() {
        sessionAuthToken.put("role", UserRole.GAA);
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
    private void setUpTestBusinesses() throws ParseException {
        testBusiness = new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("COSC co")
                .withPrimaryOwner(owner)
                .build();
        testBusiness = businessRepository.save(testBusiness);
        testBusiness.addAdmin(admin);
        testBusiness = businessRepository.save(testBusiness);

        Business business1 = new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Wellington,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("New World")
                .withPrimaryOwner(owner)
                .build();
        business1 = businessRepository.save(business1);
        business1.addAdmin(admin);
        businessRepository.save(business1);

        Business business2 = new Business.Builder()
                .withBusinessType(BusinessType.CHARITABLE)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Canberra,Australia," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Food Palace")
                .withPrimaryOwner(owner)
                .build();
        business2 = businessRepository.save(business2);
        business2.addAdmin(admin);
        businessRepository.save(business2);

        Business business3 = new Business.Builder()
                .withBusinessType(BusinessType.NON_PROFIT)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("Food Emporium")
                .withPrimaryOwner(owner)
                .build();
        business3 = businessRepository.save(business3);
        business3.addAdmin(admin);
        businessRepository.save(business3);
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
                .atStreetNumber("123")
                .onStreet("Ilam road")
                .withPostCode("8041")
                .atDistrict("Ashburton")
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
                .withPhoneNumber("64 3555012")
                .withAddress(userAddress)
                .build();
        admin = new User.Builder().withFirstName("Caroline").withMiddleName("Jane").withLastName("Smith")
                .withNickName("Carrie").withEmail("carriesmith@hotmail.com").withPassword("h375dj82")
                .withDob("2001-03-11").withPhoneNumber("64 37487562").withAddress(Location.covertAddressStringToLocation("24,Albert Road,Ashburton,Auckland,Auckland,New KZealand,0624")).build();
        otherUser = new User.Builder().withFirstName("William").withLastName("Pomeroy").withNickName("Will")
                .withEmail("pomeroy.will@outlook.com").withPassword("569277hghrud").withDob("1981-03-11")
                .withPhoneNumber("64 210995786").withAddress(Location.covertAddressStringToLocation("99,Riccarton Road,Ashburton,Christchurch,Canterbury,New Zealand,4041")).build();
        owner = userRepository.save(owner);
        admin = userRepository.save(admin);
        otherUser = userRepository.save(otherUser);
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
        assertEquals(json.getAsString("businessType"), objectMapper.convertValue(object.getBusinessType(), String.class));

        JSONObject address = new JSONObject((Map<String, ?>) json.get("address"));
        assertEquals(address.getAsString("streetNumber"), object.getAddress().getStreetNumber().toString());
        assertEquals(address.getAsString("streetName"), object.getAddress().getStreetName());
        assertEquals(address.getAsString("city"), object.getAddress().getCity());
        assertEquals(address.getAsString("region"), object.getAddress().getRegion());
        assertEquals(address.getAsString("country"), object.getAddress().getCountry());
        assertEquals(address.getAsString("postcode"), object.getAddress().getPostCode());
        assertEquals(address.getAsString("district"), object.getAddress().getDistrict());
    }

    /**
     * Test for registering a business in a blue sky scenario
     * Session logged in as the given primaryAdministratorId
     * All request values are valid
     * @throws Exception
     */
    @Test
    void RegisterBusinessTest() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"district\": \"Ashburton\",\n" +
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
    void RegisterBusinessInvalidBusinessTypeTest() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"district\": \"Ashburton\",\n" +
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
    void RegisterBusinessWhenNotLoggedIn() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"district\": \"Ashburton\",\n" +
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
     * Test for registering a business when the given primaryAdministrator doesn't
     * exist Session logged in as the given primaryAdministratorId
     * primaryAdministrator is invalid
     *
     * @throws Exception
     */
    @Test
    void RegisterBusinessInvalidIdTest() throws Exception {
        String businessJsonString =
                "{\n" +
                        "  \"primaryAdministratorId\": 999,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"district\": \"Ashburton\",\n" +
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
    void RegisterBusinessNoPermissionTest() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
                        "  \"address\": {\n" +
                        "    \"streetNumber\": \"324\",\n" +
                        "    \"streetName\": \"Ilam Road\",\n" +
                        "    \"district\": \"Ashburton\",\n" +
                        "    \"city\": \"Christchurch\",\n" +
                        "    \"region\": \"Canterbury\",\n" +
                        "    \"country\": \"New Zealand\",\n" +
                        "    \"postcode\": \"90210\"\n" +
                        "  },\n" +
                        "  \"businessType\": \"Accommodation and Food Services\"\n" +
                        "}", owner.getUserID());
        setCurrentUser(99999L);
        JSONObject businessJson = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(businessJsonString);
        mockMvc.perform(MockMvcRequestBuilders.post("/businesses").content(businessJson.toJSONString())
                .sessionAttrs(sessionAuthToken).cookie(authCookie).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    /**
     * Test that when a PUT business request is made with an empty body, a response with status code 400
     * is returned and no business is added to the database.
     */
    @Test
    void registerBusinessNoBodyTest() throws Exception {
        setCurrentUser(owner.getUserID());
        mockMvc.perform(MockMvcRequestBuilders.post("/businesses")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test that when a PUT business request is made with a body missing one of the required fields, a response with
     * status code 400 is returned and no business is added to the database.
     */
    @Test
    void registerBusinessInvalidBodyTest() throws Exception {
        String businessJsonString =
                String.format("{\n" +
                        "  \"primaryAdministratorId\": %s,\n" +
                        "  \"name\": \"Lumbridge General Store\",\n" +
                        "  \"description\": \"A one-stop shop for all your adventuring needs\",\n" +
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
                .andExpect(status().isBadRequest());
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is not logged in, the response has a 401 status code and an empty body.
     */
    @Test
    void getBusinessByIdUnauthorizedTest() throws Exception {
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", testBusiness.getId() + 1)))
        .andExpect(status().isUnauthorized()).andReturn();
        assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is logged in, but the id given in the request URL does not correspond to a
     * business in the database, the response has a 406 status code and an empty
     * body.
     */
    @Test
    void getBusinessByIdDoesNotExistTest() throws Exception{
        setCurrentUser(owner.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", 99999))
                .sessionAttrs(sessionAuthToken).cookie(authCookie)).andExpect(status().isNotAcceptable()).andReturn();
        assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is logged in as the owner of the business with the given id, the response has
     * a 200 status code and the body contains a JSON representation of the business
     * with the given id.
     *
     * @throws Exception
     */
    @Test
    void getBusinessLoggedInAsOwnerTest() throws Exception {
        testBusiness = session.find(Business.class, testBusiness.getId());
        setCurrentUser(owner.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", testBusiness.getId()))
                .sessionAttrs(sessionAuthToken).cookie(authCookie)).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        assertEquals(owner.getUserID().toString(), json.getAsString("primaryAdministratorId"));
        String adminString = json.getAsString("administrators");
        assertTrue(adminString.contains(String.format("\"id\":%d", owner.getUserID())));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(BusinessResponseDTO.withAdmins(testBusiness))),
                objectMapper.readTree(objectMapper.writeValueAsString(json))
        );
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is logged in as an admin of the business with the given id, the response has
     * a 200 status code and the body contains a JSON representation of the business
     * with the given id.
     */
    @Test
    void getBusinessLoggedInAsAdminTest() throws Exception {
        testBusiness = session.find(Business.class, testBusiness.getId());
        setCurrentUser(admin.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", testBusiness.getId()))
                .sessionAttrs(sessionAuthToken).cookie(authCookie)).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        assertNotEquals(admin.getUserID().toString(), json.getAsString("primaryAdministratorId"));
        String adminString = json.getAsString("administrators");
        assertTrue(adminString.contains(String.format("\"id\":%d", admin.getUserID())));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(BusinessResponseDTO.withAdmins(testBusiness))),
                objectMapper.readTree(objectMapper.writeValueAsString(json))
        );
    }

    /**
     * Test that when a request is made to the GET business endpoint from a user who
     * is logged in to an account which is not an owner or admin of the business
     * with the given id, the response has a 200 status code and the body contains a
     * JSON representation of the business with the given id.
     */
    @Test
    void getBusinessLoggedInAsOtherTest() throws Exception {
        testBusiness = session.find(Business.class, testBusiness.getId());
        setCurrentUser(otherUser.getUserID());
        MvcResult result = mockMvc.perform(get(String.format("/businesses/%d", testBusiness.getId()))
                .sessionAttrs(sessionAuthToken).cookie(authCookie)).andExpect(status().isOk()).andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        assertNotEquals(otherUser.getUserID().toString(), json.getAsString("primaryAdministratorId"));
        String adminString = json.getAsString("administrators");
        assertFalse(adminString.contains(String.format("\"id\":%d", otherUser.getUserID())));
        assertEquals(
                objectMapper.readTree(objectMapper.writeValueAsString(BusinessResponseDTO.withAdmins(testBusiness))),
                objectMapper.readTree(objectMapper.writeValueAsString(json))
        );
    }


    /**
     * Assert that when the owner of a business makes another user an admin,
     * the business is contained in the set of businessesAdministered for that user.
     * Assumes BusinessOwner is logged in performing the action
     * Assumes the given user is not already an admin
     * @throws Exception
     */
    @Test
    void addAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        testAdmin = session.find(User.class, testAdmin.getUserID());
        assertTrue(testAdmin.getBusinessesAdministered().contains(testBusiness));
    }

    /**
     * Assert that when logged in as the DGAA, the session can add an admin to any business
     * @throws Exception
     */
    @Test
    void addAdminWhenDGAATest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());

        loginAsDgaa();

        mockMvc.perform(put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        testAdmin = session.find(User.class, testAdmin.getUserID());
        assertTrue(testAdmin.getBusinessesAdministered().contains(testBusiness));
    }

    /**
     * Assert that when logged in as a global application admin, the session can add an admin to any business
     * @throws Exception
     */
    @Test
    void addAdminWhenGlobalApplicationAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());

        loginAsGlobalAdmin();

        mockMvc.perform(put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        testAdmin = session.find(User.class, testAdmin.getUserID());
        assertTrue(testAdmin.getBusinessesAdministered().contains(testBusiness));
    }


    /**
     * Assert that when attempting to promote a user to admin when not currently logged in
     * as the businesses' primary Owner, a 403 is returned
     * @throws Exception
     */
    @Test
    void addAdminWhenNotPrimaryOwnerTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());

        setCurrentUser(99999L); // LOGGED IN AS SOMEONE WHO IS NOT BUSINESS OWNER

        mockMvc.perform(put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
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
    void addAdminWhenNotLoggedInTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());

        mockMvc.perform(put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
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
    void addAdminWhenUserNotExistTest() throws Exception {
        Long unusedId = owner.getUserID() + admin.getUserID() + otherUser.getUserID();
        String jsonString = String.format("{\"userId\": %d}", unusedId);
        setCurrentUser(owner.getUserID());

        mockMvc.perform(put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
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
    void addAdminWhenBusinessNotExistTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(put(String.format("/businesses/%d/makeAdministrator", 99999L))
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
    void addAdminWhenUserAlreadyAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);
        testBusiness.addAdmin(testAdmin);
        testBusiness = businessRepository.save(testBusiness);
        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(put(String.format("/businesses/%d/makeAdministrator", testBusiness.getId()))
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
    void removeAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);

        testBusiness.addAdmin(testAdmin); // make user an admin
        testBusiness = businessRepository.save(testBusiness);

        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(put(String.format("/businesses/%d/removeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        testAdmin = session.find(User.class, testAdmin.getUserID());
        assertFalse(testAdmin.getBusinessesAdministered().contains(testBusiness));
    }

    /**
     * Assert that when removing a user's admin role from a business when they aren't an admin of,
     * a 400 is returned
     * @throws Exception
     */
    @Test
    void removeAdminWhenUserNotAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);

        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());
        setCurrentUser(owner.getUserID());

        mockMvc.perform(put(String.format("/businesses/%d/removeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Assert that when attempting to demote a user that does not exist,
     * a 400 is returned
     * @throws Exception
     */
    @Test
    void removeAdminWhenUserNotExistTest() throws Exception {
        String jsonString = String.format("{\"userId\": %d}", 99999L);
        setCurrentUser(owner.getUserID());

        mockMvc.perform(put(String.format("/businesses/%d/removeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Assert that as the DGAA, the session can remove an admin from any business
     * @throws Exception
     */
    @Test
    void removeAdminWhenDGAATest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);

        testBusiness.addAdmin(testAdmin); // make user an admin
        testBusiness = businessRepository.save(testBusiness);

        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());

        loginAsDgaa(); // session is setup as a DGAA now

        mockMvc.perform(put(String.format("/businesses/%d/removeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        testAdmin = session.find(User.class, testAdmin.getUserID());
        assertFalse(testAdmin.getBusinessesAdministered().contains(testBusiness));
    }

    /**
     * Assert that as a global admin, the user has permission to remove an admin from any business
     * @throws Exception
     */
    @Test
    void removeAdminWhenGlobalApplicationAdminTest() throws Exception {
        User testAdmin = new User.Builder()
                .withFirstName("Bob")
                .withMiddleName("The")
                .withLastName("Builder")
                .withNickName("Bobby")
                .withEmail("bobthebuilder@gmail.com")
                .withPassword("1337-H%nt3r2")
                .withBio("buids things")
                .withDob("2000-03-11")
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testAdmin = userRepository.save(testAdmin);
        testBusiness.addAdmin(testAdmin); // make user an admin
        testBusiness = businessRepository.save(testBusiness);

        String jsonString = String.format("{\"userId\": %d}", testAdmin.getUserID());

        loginAsGlobalAdmin(); // session is setup as a global admin now

        mockMvc.perform(put(String.format("/businesses/%d/removeAdministrator", testBusiness.getId()))
                .content(jsonString)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        testAdmin = session.find(User.class, testAdmin.getUserID());
        assertFalse(testAdmin.getBusinessesAdministered().contains(testBusiness));
    }


    @Test
    void searchOrderedByLocation_ordersByLocation() throws Exception {
        MvcResult result = mockMvc.perform(get("/businesses/search")
        .param("searchQuery", "o")
        .param("orderBy", "location")
        .sessionAttrs(sessionAuthToken)
        .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        JSONArray results = (JSONArray) response.get("results");

        JSONObject firstJsonObject = (JSONObject) results.get(0);
        JSONObject address = (JSONObject) firstJsonObject.get("address");
        String previousAddress = address.getAsString("country") + address.getAsString("city");
        for (int i = 1; i < results.size(); i++) {
            JSONObject currentBusiness = (JSONObject) results.get(i);
            address = (JSONObject) currentBusiness.get("address");
            String currentAddress = address.getAsString("country") + address.getAsString("city"); // concat country and city
            assertTrue(currentAddress.compareTo(previousAddress) >= 0);
            previousAddress = currentAddress;
        }
    }

    @Test
    void searchOrderedByName_ordersByName() throws Exception {
        MvcResult result = mockMvc.perform(get("/businesses/search")
                .param("searchQuery", "o")
                .param("orderBy", "name")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        JSONArray results = (JSONArray) response.get("results");

        JSONObject firstJsonObject = (JSONObject) results.get(0);
        String previousName = firstJsonObject.getAsString("name");
        for (int i = 1; i < results.size(); i++) {
            JSONObject currentBusiness = (JSONObject) results.get(i);
            String currentName = currentBusiness.getAsString("name");
            assertTrue(currentName.compareTo(previousName) >= 0);
            previousName = currentName;
        }
    }

    @Test
    void searchOrderedByBusinessType_ordersByType() throws Exception {
        MvcResult result = mockMvc.perform(get("/businesses/search")
                .param("searchQuery", "o")
                .param("orderBy", "businessType")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        JSONArray results = (JSONArray) response.get("results");

        JSONObject firstJsonObject = (JSONObject) results.get(0);
        String previousBusinessType = firstJsonObject.getAsString("businessType");
        for (int i = 1; i < results.size(); i++) {
            JSONObject currentBusiness = (JSONObject) results.get(i);
            String currentBusinessType = currentBusiness.getAsString("businessType");
            assertTrue(currentBusinessType.compareTo(previousBusinessType) >= 0);
            previousBusinessType = currentBusinessType;
        }
    }

    @Test
    void searchOrderedByNameReverseTrue_ordersByNameReversed() throws Exception {
        MvcResult result = mockMvc.perform(get("/businesses/search")
                .param("searchQuery", "o")
                .param("orderBy", "name")
                .param("reverse", "true")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        JSONArray results = (JSONArray) response.get("results");

        JSONObject firstJsonObject = (JSONObject) results.get(0);
        String previousName = firstJsonObject.getAsString("name");
        for (int i = 1; i < results.size(); i++) {
            JSONObject currentBusiness = (JSONObject) results.get(i);
            String currentName = currentBusiness.getAsString("name");
            assertTrue(currentName.compareTo(previousName) <= 0);
            previousName = currentName;
        }
    }

    @Test
    void searchPageTwo_returnsSecondPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/businesses/search")
                .param("searchQuery", "o")
                .param("page", "2")
                .param("resultsPerPage", "3")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        JSONArray results = (JSONArray) response.get("results");
        int count = (int) response.get("count");
        int size = results.size();

        assertEquals(1, size);
        assertEquals(4, count);
    }

    @Test
    void searchCount_returnsTotalCount() throws Exception {
        MvcResult result = mockMvc.perform(get("/businesses/search")
                .param("searchQuery", "food")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        JSONArray results = (JSONArray) response.get("results");
        int count = (int) response.get("count");

        assertEquals(2, count);
    }

    @Test
    void searchInvalidOrdering_returns400Error() throws Exception {
        mockMvc.perform(get("/businesses/search")
                .param("searchQuery", "o")
                .param("orderBy", "thisIsWrong")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchNoAuthenticationCookie_returns401Error() throws Exception {
        mockMvc.perform(get("/businesses/search")
                .param("searchQuery", "o")
                .sessionAttrs(sessionAuthToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchNoSession_returns401Error() throws Exception {
        mockMvc.perform(get("/businesses/search")
                .param("searchQuery", "o")
                .cookie(authCookie))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({"Food,,2", ",Accommodation and Food Services,2", "New World,Accommodation and Food Services,1"})
    void searchWithQueryOrTypeNotNull_matchingBusinessesInResult(String searchQuery, String businessType, int numMatches) throws Exception {
        MvcResult result = mockMvc.perform(get("/businesses/search")
                .param("searchQuery", searchQuery)
                .param("businessType", businessType)
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isOk())
                .andReturn();

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        int count = (int) response.get("count");
        assertEquals(numMatches, count);
    }

    @Test
    void searchWithQueryAndTypeNull_returns400Error() throws Exception {
        mockMvc.perform(get("/businesses/search")
                .sessionAttrs(sessionAuthToken)
                .cookie(authCookie))
                .andExpect(status().isBadRequest());
    }
}
