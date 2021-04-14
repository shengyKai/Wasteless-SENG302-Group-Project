package org.seng302.Entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.seng302.Exceptions.AccessTokenException;
import org.seng302.Persistence.BusinessRepository;
import org.seng302.Persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.text.ParseException;
import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class BusinessTests {

    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    UserRepository userRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;

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

        MockitoAnnotations.openMocks(this);

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
     * Test that the checkSessionPermissions method will throw an AccessTokenException when called
     * with a HTTP request that does not contain an authentication token (i.e. the user has not logged in).
     */
    @Test
    public void checkSessionPermissionsNoAuthenticationTokenTest() {
        when(request.getSession()).thenAnswer(
                (Answer) invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                (Answer) invocation -> null);
        assertThrows(AccessTokenException.class, () -> {
            testBusiness1.checkSessionPermissions(request);
        });
    }

    /**
     * Test that the checkSessionPermissions method will throw a ResponseStatusException with status
     * code 403 when called with a request from a user who is not an admin of the business or a global
     * application admin.
     */
    @Test
    public void checkSessionPermissionsUserWithoutPermissionTest() {
        Long user2Id = userRepository.findByEmail("dave@gmail.com").getUserID();
        when(request.getSession()).thenAnswer(
                (Answer) invocation -> session);
        when(request.getSession(false)).thenAnswer(
                (Answer) invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                (Answer) invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                (Answer) invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
            (Answer) invocation -> "user");
        when(session.getAttribute("accountId")).thenAnswer(
            (Answer) invocation -> user2Id);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            testBusiness1.checkSessionPermissions(request);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    /**
     * Test that when the checkSessionPermissions method is called with a request from a user who is
     * an admin of the business, no exception is thrown.
     */
    @Test
    public void checkSessionPermissionsBusinessAdminTest() {
        Long user1Id = userRepository.findByEmail("johnsmith99@gmail.com").getUserID();
        when(request.getSession()).thenAnswer(
                (Answer) invocation -> session);
        when(request.getSession(false)).thenAnswer(
                (Answer) invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                (Answer) invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                (Answer) invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
            (Answer) invocation -> "user");
        when(session.getAttribute("accountId")).thenAnswer(
            (Answer) invocation -> user1Id);
        try {
            testBusiness1.checkSessionPermissions(request);
        } catch (Exception e) {
            System.out.println(Arrays.toString(testBusiness.getAdministrators().toArray()));
            System.out.println(testBusiness.getPrimaryOwner().toString());
            System.out.println(user1Id);
            System.out.println(e);
            fail("No exception should be thrown when the user is an admin of the business");
        }
    }

        /**
     * Test that when the checkSessionPermissions method is called with a request from a user who is
     * a global application admin, no exception is thrown.
     */
    @Test
    public void checkSessionPermissionsGlobalApplicationAdminTest() {
        Long user1Id = userRepository.findByEmail("dave@gmail.com").getUserID();
        when(request.getSession()).thenAnswer(
                (Answer) invocation -> session);
        when(request.getSession(false)).thenAnswer(
                (Answer) invocation -> session);
        when(session.getAttribute("AUTHTOKEN")).thenAnswer(
                (Answer) invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                (Answer) invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie("AUTHTOKEN", "abcd1234");
                    return cookieArray;
                });
        when(session.getAttribute("role")).thenAnswer(
            (Answer) invocation -> "admin");
        when(session.getAttribute("accountId")).thenAnswer(
            (Answer) invocation -> user1Id);
        try {
            testBusiness1.checkSessionPermissions(request);
        } catch (Exception e) {
            fail("No exception should be thrown when the user is a global application admin");
        }
    }
}
