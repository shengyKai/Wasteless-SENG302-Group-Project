package org.seng302.Tools;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.stubbing.Answer;
import org.seng302.Exceptions.AccessTokenException;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class AuthenticationTokenManagerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Captor
    ArgumentCaptor<Cookie> cookieArgumentCaptor;

    @Captor
    ArgumentCaptor<String> nameArgumentCaptor;

    @Captor
    ArgumentCaptor<String> valueArgumentCaptor;

    private final String authTokenName = "AUTHTOKEN";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Verify that when setAuthenticationToken is called a cookie with name "JSESSIONID" is added to the HTTP response.
     */
    // Todo: Fix or replace this test once spring security is working
    @Test
    public void setAuthenticationTokenCookieAddedTest() {
        when(request.getSession(true)).thenAnswer(
                (Answer) invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response, null);
        Mockito.verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie responseCookie = cookieArgumentCaptor.getValue();
        assertEquals("AUTHTOKEN", responseCookie.getName());
    }

    /**
     * Verify that when setAuthenticationToken is called the JSESSIONID cookie contains a 32 character hexidemcimal string.
     */
    @Test
    public void setAuthenticationTokenCookieValueTest() {
        when(request.getSession(true)).thenAnswer(
                (Answer) invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response, null);
        Mockito.verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie responseCookie = cookieArgumentCaptor.getValue();
        assertTrue(responseCookie.getValue().matches("[abcdef\\d]{32}"));
    }

    /**
     * Verify that when setAuthenticationToken is called the JSESSIONID cookie is set to expire in 30 minutes.
     */
    @Test
    public void setAuthenticationTokenCookieExpiryTest() {
        when(request.getSession(true)).thenAnswer(
                (Answer) invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response, null);
        Mockito.verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie responseCookie = cookieArgumentCaptor.getValue();
        assertEquals(30 * 60, responseCookie.getMaxAge());
    }

    /**
     * Verify that when setAuthenticationToken is called an attribute with name JSESSIONID is added to the session.
     */
    // Todo: fix or replace this test once we have got spring security working
    @Test
    public void setAuthenticationTokenSessionAttributeAddedTest() {
        when(request.getSession(true)).thenAnswer(
                (Answer) invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response, null);
        Mockito.verify(session).setAttribute(nameArgumentCaptor.capture(), valueArgumentCaptor.capture());
        String attributeName = nameArgumentCaptor.getValue();
        assertEquals("AUTHTOKEN", attributeName);
    }

    /**
     * Verify that when setAuthenticationToken is called the value of the cookie JSESSIONID added to the response is the
     * same as the value of the attribute JSESSIONID added to the session.
     */
    @Test
    public void setAuthenticationTokenSessionAttributeCookieMatchTest() {
        when(request.getSession(true)).thenAnswer(
                (Answer) invocation -> session);
        AuthenticationTokenManager.setAuthenticationToken(request, response, null);
        Mockito.verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie responseCookie = cookieArgumentCaptor.getValue();
        Mockito.verify(session).setAttribute(nameArgumentCaptor.capture(), valueArgumentCaptor.capture());
        String attributeValue = valueArgumentCaptor.getValue();
        assertEquals(responseCookie.getValue(), attributeValue);
    }

    /**
     * Verify that when checkAuthenticationToken is called and the cookie JSESSIONID is not present in the request an
     * AccessTokenException is thrown.
     */
    @Test
    public void checkAuthenticationTokenCookieNotPresentTest() {
        when(request.getSession()).thenAnswer(
                (Answer) invocation -> session);
        when(session.getAttribute(authTokenName)).thenAnswer(
                (Answer) invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                (Answer) invocation -> new Cookie[0]);
        assertThrows(AccessTokenException.class, () -> {
            AuthenticationTokenManager.checkAuthenticationToken(request);
        });
    }

    /**
     * Verify that when checkAuthenticationToken is called and the attribute JSESSIONID does not exist for this session
     * an AccessTokenException is thrown.
     */
    @Test
    public void checkAuthenticationTokenSessionAttributeNotPresentTest() {
        when(request.getSession()).thenAnswer(
                (Answer) invocation -> session);
        when(session.getAttribute(authTokenName)).thenAnswer(
                (Answer) invocation -> null);
        when(request.getCookies()).thenAnswer(
                (Answer) invocation -> new Cookie[0]);
        assertThrows(AccessTokenException.class, () -> {
            AuthenticationTokenManager.checkAuthenticationToken(request);
        });
    }

    /**
     * Verify that when checkAuthenticationToken is called and the value of the cookie JSESSIONID does not match the
     * value of the attribute JSESSIONID for this session an AccessTokenException is thrown.
     */
    @Test
    public void checkAuthenticationTokenNoMatchTest() {
        when(request.getSession()).thenAnswer(
                (Answer) invocation -> session);
        when(session.getAttribute(authTokenName)).thenAnswer(
                (Answer) invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                (Answer) invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie(authTokenName, "1234abcd");
                    return cookieArray;
                });
        assertThrows(AccessTokenException.class, () -> {
            AuthenticationTokenManager.checkAuthenticationToken(request);
        });
    }

    /**
     * Verify that when checkAuthenticationToken is called and the value of the cookie JSESSIONID matches the
     * value of the attribute JSESSIONID for this session an no exception is thrown.
     */
    @Test
    public void checkAuthenticationTokenMatchTest() {
        when(request.getSession()).thenAnswer(
                (Answer) invocation -> session);
        when(session.getAttribute(authTokenName)).thenAnswer(
                (Answer) invocation -> "abcd1234");
        when(request.getCookies()).thenAnswer(
                (Answer) invocation -> {
                    Cookie[] cookieArray = new Cookie[1];
                    cookieArray[0] = new Cookie(authTokenName, "abcd1234");
                    return cookieArray;
                });
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            assertTrue(true);
        } catch (RuntimeException e) {
            fail("Exception should not be thrown when stored authentication token matches authentication token in cookie");
        }
    }

}