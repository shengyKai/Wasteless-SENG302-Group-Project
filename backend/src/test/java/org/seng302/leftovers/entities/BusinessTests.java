package org.seng302.leftovers.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.dto.LocationDTO;
import org.seng302.leftovers.dto.business.BusinessResponseDTO;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.dto.business.Rank;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.dto.user.UserRole;
import org.seng302.leftovers.exceptions.AccessTokenResponseException;
import org.seng302.leftovers.exceptions.InsufficientPermissionResponseException;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class BusinessTests {

    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private ObjectMapper objectMapper;

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private Business testBusiness1;

    /**
     * Sets up 3 Users and 1 Business
     * User1 is primary owner of the business
     * @throws ParseException
     */
    @BeforeEach
    void setUp() throws ParseException {
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
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
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
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
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
                .withPhoneNumber("64 3555012")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testUser3 = userRepository.save(testUser3);
        testBusiness1 = new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("BusinessName")
                .withPrimaryOwner(testUser1)
                .build();
        testBusiness1 = businessRepository.save(testBusiness1);

        MockitoAnnotations.openMocks(this);

    }

    @AfterEach
    void tearDown() {
        businessRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
        imageRepository.deleteAll();
    }

    /**
     * Test that when a User is an admin of a business, the set of business admins contains that user
     * @throws Exception
     */
    @Test
    void getAdministratorsReturnsAdministrators() throws Exception {
        testBusiness1.addAdmin(testUser2);
        testBusiness1 = businessRepository.save(testBusiness1);
        assertTrue(testBusiness1.getAdministrators().stream().anyMatch(user -> user.getUserID() == testUser2.getUserID()));
    }

    /**
     * Test that getting the primary owner correctly returns the primary owner
     * @throws Exception
     */
    @Test
    void getPrimaryOwnerReturnsPrimaryOwner() throws Exception {
        assertEquals(testUser1.getUserID(), testBusiness1.getPrimaryOwner().getUserID());
    }

    /**
     * No errors should be thrown when the type is a valid business type
     * @throws ResponseStatusException
     */
    @Test
    void setBusinessTypeWhenValid() throws ResponseStatusException {
        testBusiness1.setBusinessType(BusinessType.RETAIL_TRADE);
        assertEquals(BusinessType.RETAIL_TRADE, testBusiness1.getBusinessType());
    }

    /**
     * Test that setting the business name changes the business name
     */
    @Test
    void setNameSetsName() {
        String newName = "Cool Business";
        testBusiness1.setName(newName);
        assertEquals(newName, testBusiness1.getName());
    }

    /**
     * test that setting the business description sets the business description
     */
    @Test
    void setDescriptionSetsDescription() {
        String newDesc = "Some description";
        testBusiness1.setDescription(newDesc);
        assertEquals(newDesc, testBusiness1.getDescription());
    }

    /**
     * tests that if user is above or equal 16 years old, the user's data will be stored as the primary owner
     */
    @Test
    void setAboveMinimumAge() {
        assertNotNull(testBusiness1.getPrimaryOwner());
    }

    /**
     * tests that if user is below 16 years old, an exception will be thrown
     */
    @Test
    void setBelowMinimumAge() {
        Business.Builder builder = new Business.Builder()
                .withBusinessType(BusinessType.ACCOMMODATION_AND_FOOD_SERVICES)
                .withAddress(
                        Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .withDescription("Some description")
                .withName("BusinessName")
                .withPrimaryOwner(testUser3);
        Exception thrown = assertThrows(InsufficientPermissionResponseException.class, builder::build, "Expected Business.builder() to throw, but it didn't");
        assertTrue(thrown.getMessage().contains("User is not of minimum age required to create a business"));
    }

    /**
     * Helper function that returns an array of valid names to use in tests.
     *
     * @return An array of valid business names various lengths and character types
     */
    private String[] getTestNames() {
        return new String[]{"Joe's cake shop", "BNZ", "X", "cool business", "big-business", "$%@&.,:;", "BUSINESS123", "" +
                "another_business", "a".repeat(100)};
    }


    /**
     * Check that when setName is called with a name which is 100 characters or less, contains only letters, numbers and
     * the characters "@ $ % & . , ' ; : - _", and is not empty, the businesses name is set to that value.
     */
    @Test
    void setNameValidNameTest() {
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
    void setNameSavedToRepositoryTest() {
        String[] testNames = getTestNames();
        for (String name : testNames) {
            testBusiness1.setName(name);
            businessRepository.save(testBusiness1);
            testBusiness1 = businessRepository.findById(testBusiness1.getId()).orElseThrow();
            assertEquals(name, testBusiness1.getName());
        }
    }

    /**
     * Check that when setName is called with a name which contains characters which are not letters, numbers or
     * the characters "! " # $ % & ' ( ) * + , - . / : ; < = > ? @ [ \ ] ^ _ ` { | } ~",
     * a response status exception with status code 400 will be thrown and the
     * business's name will not be changed.
     */
    @Test
    void setNameInvalidCharacterTest() {
        String originalName = testBusiness1.getName();
        String[] invalidCharacterNames = {"\n", "»»»»»", "business¢", "½This is not allowed", "¡or this¡"};
        for (String name : invalidCharacterNames) {
            var e = assertThrows(ValidationResponseException.class, () -> {
                testBusiness1.setName(name);
            });
            assertEquals(originalName, testBusiness1.getName());
        }
    }

    /**
     * Check that when setName is called with a name which is greater than 100 characters lonog, a response status
     * exception with status code 400 will be thrown and the business's name will not be changed.
     */
    @Test
    void setNameTooLongTest() {
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
            var e = assertThrows(ValidationResponseException.class, () -> {
                testBusiness1.setName(name);
            });
            assertEquals(originalName, testBusiness1.getName());
        }
    }

    /**
     * Check that when setName is called with null as its argument, a response status expection will be thrown with
     * status code 400 and the business's name will not be changed.
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void setNameToInvalidValue(String value) {
        String originalName = testBusiness1.getName();
        var e = assertThrows(ValidationResponseException.class, () -> testBusiness1.setName(value));
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
    void setDescriptionValidDescriptionTest() {
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
    void setDescriptionSavedToRepositoryTest() {
        String[] testDescriptions = getTestDescriptions();
        for (String description : testDescriptions) {
            testBusiness1.setDescription(description);
            businessRepository.save(testBusiness1);
            testBusiness1 = businessRepository.findById(testBusiness1.getId()).orElseThrow();
            assertEquals(description, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called with a name which contains characters which are not letters, numbers or
     * the characters "@ $ % & . , ; : - _", a response status exception with status code 400 will be thrown and the
     * business's description will not be changed.
     */
    @Test
    void setDescriptionInvalidCharacterTest() {
        String originalDescription = testBusiness1.getDescription();
        String[] invalidCharacterDescriptions = {"»»»»»", "business¢", "½This is not allowed", "¡or this¡"};
        for (String description : invalidCharacterDescriptions) {
            var e = assertThrows(ValidationResponseException.class, () -> {
                testBusiness1.setDescription(description);
            });
            assertEquals(originalDescription, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called with a string which is greater than 200 characters lonog, a response status
     * exception with status code 400 will be thrown and the business's description will not be changed.
     */
    @Test
    void setDescriptionTooLongTest() {
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
            var e = assertThrows(ValidationResponseException.class, () -> {
                testBusiness1.setDescription(description);
            });
            assertEquals(originalDescription, testBusiness1.getDescription());
        }
    }

    /**
     * Check that when setDescription is called with null as its argument, the description becomes an empty string
     */
    @Test
    void setDescriptionNullTest() {
        testBusiness1.setDescription(null);
        assertEquals("", testBusiness1.getDescription());
    }

    /**
     * Check that if someone attempts to remove a user who is currently the primary owner of a business from the database,
     * an expection is thrown and the user is not removed from the database.
     */
    @Test
    void primaryOwnerCantBeDeletedTest() {
        User primaryOwner = testBusiness1.getPrimaryOwner();
        long userId = primaryOwner.getUserID();
        assertThrows(ValidationResponseException.class, () -> {
            userRepository.deleteById(userId);
        });
        assertTrue(userRepository.findById(primaryOwner.getUserID()).isPresent());
    }

    /**
     * Check that if addAdmin is called with a user who is already an admin of the business, a response status expection
     * with status code 400 is thrown adn the admin is not added.
     */
    @Test
    void addAdminCurrentAdminTest() {
        testBusiness1.addAdmin(testUser2);
        var e = assertThrows(ValidationResponseException.class, () -> {
            testBusiness1.addAdmin(testUser2);
        });
        assertEquals(1, testBusiness1.getAdministrators().size());
        assertTrue(testBusiness1.getAdministrators().contains(testUser2));
    }

    /**
     * Check that if addAdmin is called with a user who is already the primary owner of the business, a response status
     * expection with status code 400 is thrown and the admin is not added.
     */
    @Test
    void addAdminPrimaryOwnerTest() {
        var e = assertThrows(ValidationResponseException.class, () -> {
            testBusiness1.addAdmin(testUser1);
        });
        assertEquals(0, testBusiness1.getAdministrators().size());
    }

    /**
     * Check that if addAdmin is called with a user who is not currently an admin of the business, that user is added to
     * the businesses list of administrators.
     */
    @Test
    void addAdminNewAdminTest() {
        testBusiness1.addAdmin(testUser2);
        assertEquals(1, testBusiness1.getAdministrators().size());
        assertTrue(testBusiness1.getAdministrators().contains(testUser2));
    }

    /**
     * Check that if someone attempts to delete a user who is currently an admin of a business from the database,
     * a DataIntegrityVioloationException will be thrown, and the user and business will not be changed.
     */
    @Test
    void adminDeletedTest() {
        testBusiness1.addAdmin(testUser2);
        testBusiness1 = businessRepository.save(testBusiness1);
        long adminId = testUser2.getUserID();
        long businessId = testBusiness1.getId();
        userRepository.deleteById(testUser2.getUserID());
        assertFalse(userRepository.existsById(adminId));
        assertTrue(businessRepository.existsById(businessId));
        testBusiness1 = businessRepository.findById(businessId).orElseThrow();
        assertEquals(0, testBusiness1.getAdministrators().size());
    }

    /**
     * Test that when a business which has not admins is deleted, that business is removed from the repository
     * but its primary owner entity is not deleted.
     */
    @Test
    void deleteBusinessWithoutAdminsTest() {
        long businessId = testBusiness1.getId();
        long ownerId = testBusiness1.getPrimaryOwner().getUserID();
        businessRepository.deleteById(businessId);
        assertFalse(businessRepository.existsById(businessId));
        assertTrue(userRepository.existsById(ownerId));

        try (Session session = sessionFactory.openSession()) {
            assertTrue(session.find(User.class, ownerId).getBusinessesOwned().isEmpty());
        }
    }

    /**
     * Test that when delete is called on a business which has administrators, a DataIntegrityViolationException
     * is thrown and the business and its admins remain in the database.
     */
    private void deleteBusinessWithAdminsTest() {
        testBusiness1.addAdmin(testUser2);
        testBusiness1 = businessRepository.save(testBusiness1);
        long adminId = testUser2.getUserID();
        long businessId = testBusiness1.getId();
        assertThrows(DataIntegrityViolationException.class, () -> {
            businessRepository.deleteById(businessId);
        });
        assertTrue(userRepository.existsById(adminId));
        assertTrue(businessRepository.existsById(businessId));
        testBusiness1 = businessRepository.findById(businessId).orElseThrow();
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
    void setCreatedInitialValueTest() {
        Business testBusiness2 = new Business.Builder()
                .withBusinessType(BusinessType.NON_PROFIT)
                .withName("Zesty Business")
                .withAddress(Location.covertAddressStringToLocation("101,My Street,Ashburton,Christchurch,Canterbury,New Zealand,1010"))
                .withDescription("A nice place")
                .withPrimaryOwner(testUser2)
                .build();
        assertTrue(ChronoUnit.SECONDS.between(Instant.now(), testBusiness2.getCreated()) < 20);
    }

    /**
     * Test that when setAddress is called with null as the address, a response status exception with status code 400
     * will be thrown and the business's address will not be changed.
     */
    @Test
    void setAddressNullTest() {
        Location originalAddress = testBusiness1.getAddress();
        var e = assertThrows(ValidationResponseException.class, () -> {
            testBusiness1.setAddress(null);
        });
        assertEquals(originalAddress, testBusiness1.getAddress());
    }

    /**
     * Test than when setAddress is called with a Location object as the argument, the business's address will be set to
     * the given location.
     */
    @Test
    void setAddressValidTest() {
        Location address = Location.covertAddressStringToLocation("44,Humbug Ave,Ashburton,Hamilton,Waikato,New Zealand,1000");
        testBusiness1.setAddress(address);
        assertEquals(address, testBusiness1.getAddress());
    }


    /**
     * Helper function for constructJson tests. Creates a list containing JSONObject returned by
     * constructJson method when called on the given business with true, false or no arguement.
     * @return A lsit of JSONObjects produced by calling constructJson with true, false and no arg
     */
    private List<JSONObject> getTestJsons(Business business) {
        List<BusinessResponseDTO> testDTOs = List.of(BusinessResponseDTO.withAdmins(business), BusinessResponseDTO.withoutAdmins(business));
        return objectMapper.convertValue(testDTOs, new TypeReference<>() {});
    }

    /**
     * Test that the JSONObject returned by constructJson contains the fields id, primaryAdministratorId,
     * name, description, businessType, created, administrators and address when constructJson is
     * called with true as its arguement.
     */
    @Test
    void constructJsonHasExpectedFieldsFullDetailsTrueTest() {
        var json = objectMapper.convertValue(BusinessResponseDTO.withAdmins(testBusiness1), JSONObject.class);
       assertTrue(json.containsKey("name"));
       assertTrue(json.containsKey("description"));
       assertTrue(json.containsKey("businessType"));
       assertTrue(json.containsKey("address"));
       assertTrue(json.containsKey("id"));
       assertTrue(json.containsKey("primaryAdministratorId"));
       assertTrue(json.containsKey("administrators"));
       assertTrue(json.containsKey("created"));
       assertTrue(json.containsKey("points"));
       assertTrue(json.containsKey("rank"));
    }

    /**
     * Test that the JSONObject returned by constructJson contains the fields id, primaryAdministratorId,
     * name, description, businessType, created and address when constructJson is
     * called with false or no argument.
     */
    @Test
    void constructJsonHasExpectedFieldsFullDetailsFalseTest() {
        var json = objectMapper.convertValue(BusinessResponseDTO.withoutAdmins(testBusiness1), JSONObject.class);
        assertTrue(json.containsKey("name"));
        assertTrue(json.containsKey("description"));
        assertTrue(json.containsKey("businessType"));
        assertTrue(json.containsKey("address"));
        assertTrue(json.containsKey("id"));
        assertTrue(json.containsKey("primaryAdministratorId"));
        assertTrue(json.containsKey("created"));
        assertTrue(json.containsKey("points"));
        assertTrue(json.containsKey("rank"));
    }

    /**
     * Test that the JSONObject returned by contructJson does not contain any fields other than
     * id, primaryAdministratorId, name, description, businessType, created, administraters and address,
     * whether constructJson is called with true as its argument.
     */
    @Test
    void constructJsonDoesntHaveUnexpectedFieldsFullDetailsTrueTest() {
        JSONObject json = objectMapper.convertValue(BusinessResponseDTO.withAdmins(testBusiness1), JSONObject.class);
        json.remove("name");
        json.remove("description");
        json.remove("businessType");
        json.remove("address");
        json.remove("id");
        json.remove("primaryAdministratorId");
        json.remove("administrators");
        json.remove("created");
        json.remove("images");
        json.remove("points");
        json.remove("rank");
        assertTrue(json.isEmpty());
    }

    /**
     * Test that the JSONObject returned by contructJson does not contain any fields other than
     * id, primaryAdministratorId, name, description, businessType, created and address,
     * whether constructJson is called with false or no argument.
     */
    @Test
    void constructJsonDoesntHaveUnexpectedFieldsFullDetailsFalseTest() {
        var json = objectMapper.convertValue(BusinessResponseDTO.withoutAdmins(testBusiness1), JSONObject.class);
        json.remove("name");
        json.remove("description");
        json.remove("businessType");
        json.remove("address");
        json.remove("id");
        json.remove("primaryAdministratorId");
        json.remove("created");
        json.remove("images");
        json.remove("points");
        json.remove("rank");
        assertTrue(json.isEmpty());
    }

    /**
     * Test that id, primaryAdministratorId, name, description, businessType, created, and address
     * in the JSONObject returned by constructJson have the expecte value, whether cosntructJson is
     * called with true, false or no argument.
     */
    @Test
    void constructJsonSimpleFieldsHaveExpectedValueTest() throws JsonProcessingException {
        List<JSONObject> testJsons = getTestJsons(testBusiness1);
        for (var json : testJsons) {
            assertEquals(testBusiness1.getName(), json.getAsString("name"));
            assertEquals(testBusiness1.getDescription(), json.getAsString("description"));
            assertEquals(testBusiness1.getBusinessType(), objectMapper.convertValue(json.get("businessType"), BusinessType.class));
            assertEquals(new LocationDTO(testBusiness1.getAddress(), true), objectMapper.convertValue(json.get("address"), LocationDTO.class));
            assertEquals(testBusiness1.getId().toString(), json.getAsString("id"));
            assertEquals(testBusiness1.getPrimaryOwner().getUserID().toString(), json.getAsString("primaryAdministratorId"));
            assertEquals(testBusiness1.getCreated().toString(), json.getAsString("created"));
            assertEquals(List.of(), json.get("images"));
            assertEquals(testBusiness1.getPoints(), json.get("points"));
            assertEquals(
                    objectMapper.readTree(objectMapper.writeValueAsString(testBusiness1.getRank())),
                    objectMapper.readTree(objectMapper.writeValueAsString(json.get("rank"))));
        }
    }

    /**
     * Test that when constructJson is called with true as its argument, the administrators field
     * contains a list of User JSONs with the details of the business's administrators.
     */
    @Test
    void constructJsonAdministratorsFullDetailsTest() throws JsonProcessingException {
        testBusiness1.addAdmin(testUser2);
        assertEquals(2, testBusiness1.getOwnerAndAdministrators().size());
        List<User> admins = new ArrayList<>(testBusiness1.getOwnerAndAdministrators());
        admins.sort(Comparator.comparing(Account::getUserID));

        List<UserResponseDTO> expectedAdminArray = admins.stream().map(UserResponseDTO::new).collect(Collectors.toList());

        var json = objectMapper.convertValue(BusinessResponseDTO.withAdmins(testBusiness1), JSONObject.class);
        List<UserResponseDTO> actualAdminArray = objectMapper.convertValue(json.get("administrators"), new TypeReference<>() {});

        assertEquals(expectedAdminArray, actualAdminArray);
    }

    /**
     * Test that when the business has a primary owner but not administrators, the getOwnerAndAdministrators
     * method will return a set containing just the business's primary owner.
     */
    @Test
    void getOwnerAndAdministratorsNoAdministratorsTest() {
        assertEquals(1, testBusiness1.getOwnerAndAdministrators().size());
        assertTrue(testBusiness1.getOwnerAndAdministrators().contains(testUser1));
    }

    /**
     * Test that when the business has administrators added in addition to its primary owner, the
     * getOwnerAndAdministrators method will return a set containing the owner and all administrators
     * of the business.
     */
    @Test
    void getOwnerAndAdministratorsAdminsAddedTest() {
        testBusiness1.addAdmin(testUser2);
        assertEquals(2, testBusiness1.getOwnerAndAdministrators().size());
        assertTrue(testBusiness1.getOwnerAndAdministrators().contains(testUser1));
        assertTrue(testBusiness1.getOwnerAndAdministrators().contains(testUser2));
    }




    /**
     * Test that the checkSessionPermissions method will throw an AccessTokenResponseException when called
     * with a HTTP request that does not contain an authentication token (i.e. the user has not logged in).
     */
    @Test
    void checkSessionPermissionsNoAuthenticationTokenTest() {
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> null);
        assertThrows(AccessTokenResponseException.class, () -> {
            testBusiness1.checkSessionPermissions(request);
        });
    }

    /**
     * Test that the checkSessionPermissions method will throw a ResponseStatusException with status
     * code 403 when called with a request from a user who is not an admin of the business or a global
     * application admin.
     */
    @Test
    void checkSessionPermissionsUserWithoutPermissionTest() {
        Long user2Id = userRepository.findByEmail("dave@gmail.com").getUserID();
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(request.getSession(false)).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
            invocation -> "user");
        when(session.getAttribute("accountId")).thenAnswer(
            invocation -> user2Id);
        var exception = assertThrows(InsufficientPermissionResponseException.class, () -> {
            testBusiness1.checkSessionPermissions(request);
        });
    }

    /**
     * Test that when the checkSessionPermissions method is called with a request from a user who is
     * an admin of the business, no exception is thrown.
     */
    @Test
    void checkSessionPermissionsBusinessAdminTest() {
        Long user1Id = userRepository.findByEmail("johnsmith99@gmail.com").getUserID();
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(request.getSession(false)).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
            invocation -> "user");
        when(session.getAttribute("accountId")).thenAnswer(
            invocation -> user1Id);
        try {
            testBusiness1.checkSessionPermissions(request);
        } catch (Exception e) {
            fail("No exception should be thrown when the user is an admin of the business");
        }
    }

    /**
     * Test that when the checkSessionPermissions method is called with a request from a user who is
     * a global application admin, no exception is thrown.
     */
    @Test
    void checkSessionPermissionsGlobalApplicationAdminTest() {
        Long user2Id = userRepository.findByEmail("dave@gmail.com").getUserID();
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(request.getSession(false)).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
            invocation -> UserRole.GAA);
        when(session.getAttribute("accountId")).thenAnswer(
            invocation -> user2Id);
        try {
            testBusiness1.checkSessionPermissions(request);
        } catch (Exception e) {
            fail("No exception should be thrown when the user is a global application admin");
        }
    }

    /**
     * Test that when the checkSessionPermissions method is called with a request from a user who is
     * a global application admin, no exception is thrown.
     */
    @Test
    void checkSessionPermissionsDefaultGlobalApplicationAdminTest() {
        when(request.getSession()).thenAnswer(
                invocation -> session);
        when(request.getSession(false)).thenAnswer(
                invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
                invocation -> UserRole.DGAA);
        when(session.getAttribute("accountId")).thenAnswer(
                invocation -> null);
        try {
            testBusiness1.checkSessionPermissions(request);
        } catch (Exception e) {
            fail("No exception should be thrown when the user is the default global application admin");
        }
    }

    @Test
    void getCatalogue_multipleImages_noDuplicates() {
        Image testImage1 = imageRepository.save(new Image("filename1.png", "thumbnailFilename1.png"));
        Image testImage2 = imageRepository.save(new Image("filename2.png", "thumbnailFilename2.png"));
        Image testImage3 = imageRepository.save(new Image("filename3.png", "thumbnailFilename3.png"));
        Image testImage4 = imageRepository.save(new Image("filename4.png", "thumbnailFilename4.png"));

        Product testProduct1 = new Product.Builder()
            .withName("Product 1")
            .withProductCode("PROD1")
            .withBusiness(testBusiness1)
            .build();
        testProduct1 = productRepository.save(testProduct1);      
        testProduct1.addImage(testImage1);
        testProduct1.addImage(testImage2);
        testProduct1 = productRepository.save(testProduct1);
          
        Product testProduct2 = new Product.Builder()
            .withName("Product 2")
            .withProductCode("PROD2")
            .withBusiness(testBusiness1)
            .build();
        testProduct2 = productRepository.save(testProduct2);
        testProduct2.addImage(testImage3);
        testProduct2.addImage(testImage4);
        testProduct2 = productRepository.save(testProduct2);
        

        testBusiness1 = businessRepository.save(testBusiness1);
        assertEquals(2, productRepository.findAllByBusiness(testBusiness1).size());
    }

    @ParameterizedTest
    @CsvSource({
            "ACCOMMODATION_AND_FOOD_SERVICES,Accommodation and Food Services",
            "RETAIL_TRADE,Retail Trade",
            "CHARITABLE,Charitable organisation",
            "NON_PROFIT,Non-profit organisation"
    })
    void businessType_toString_isExpectedString(String typeString, String mappedTypeString) {
        BusinessType role = BusinessType.valueOf(typeString);
        assertEquals(mappedTypeString, objectMapper.convertValue(role, String.class));
    }

    @Test
    void incrementPoints_incrementsPoints() {
        var pointsInitial = testBusiness1.getPoints();
        testBusiness1.incrementPoints();
        assertEquals(pointsInitial + 1,testBusiness1.getPoints());
    }

    @Test
    void businessCreated_rankIsBronze() {
        assertEquals(Rank.BRONZE, testBusiness1.getRank());
    }
}
