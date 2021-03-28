package org.seng302.Tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Exceptions.AccessTokenException;
import org.seng302.Exceptions.InsufficientPermissionException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;

/**
 * This class provides static methods to generate and verify the authentication tokens used to check whether a user is
 * logged in.
 */
public class AuthenticationTokenManager {

    private static final String authTokenName = "AUTHTOKEN";
    //private static final String authTokenName = "JSESSIONID";
    private static final Logger logger = LogManager.getLogger(AuthenticationTokenManager.class.getName());

    /**
     * This method sets the authentication token for this session and constructs a cookie containing the authentication
     * token which will be attached to the HTTP response.
     * @param request The HTTP request packet.
     * @param response The HTTP response packet.
     */
    public static String setAuthenticationToken(HttpServletRequest request, HttpServletResponse response) {
        String authString = generateAuthenticationString();

        // Cookie currently not being used
        Cookie authToken = new Cookie(authTokenName, authString);
        authToken.setPath("/");
        authToken.setHttpOnly(true);
        authToken.setMaxAge(30 * 60);   // Set cookie expiry for 30 minutes from now
        response.addCookie(authToken);

        HttpSession session = request.getSession(true);
        session.setAttribute(authTokenName, authString);

        // Sends auth token to be sent in the response body
        String authJSONString = "{\"authToken\": \"" + authString + "\"}";
        return authJSONString;
    }

    /**
     * This method generates a random 16-byte code an converts it to a hexidecimal string which can be used as the
     * authentication code for the session.
     * @return A 32-character string representing 16 random bytes in hexidecimal notation.
     */
    private static String generateAuthenticationString() {
        SecureRandom random = new SecureRandom();
        byte[] authBytes = new byte[16];
        random.nextBytes(authBytes);
        return PasswordAuthenticator.byteArrayToHexString(authBytes);
    }

    /**
     * This method checks that the authentication token for the HTTP request matches the expected token for this session.
     * @param request The HTTP request packet.
     */
    public static void checkAuthenticationToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String expectedAuthString = (String) session.getAttribute(authTokenName);
        if (expectedAuthString == null) {
            AccessTokenException accessTokenException = new AccessTokenException("Access token not present for session.");
            logger.error(accessTokenException.getMessage());
            throw accessTokenException;
        }
        Cookie[] requestCookies = request.getCookies();
        for (Cookie cookie : requestCookies) {
            if (cookie.getName().equals(authTokenName)) {
                if (cookie.getValue().equals(session.getAttribute(authTokenName))) {
                    return;
                } else {
                    AccessTokenException accessTokenException = new AccessTokenException("Invalid access token.");
                    logger.error(accessTokenException.getMessage());
                    throw accessTokenException;
                }
            }
        }
        AccessTokenException accessTokenException = new AccessTokenException("Access token not present in request.");
        logger.error(accessTokenException.getMessage());
        throw accessTokenException;
    }

    /**
     * This method tags the current session as a DGAA session
     * @param request The HTTP request packet
     */
    public static void setAuthenticationTokenDGAA(HttpServletRequest request) {

        HttpSession session = request.getSession(true);
        session.setAttribute("dgaa", true);
    }

    /**
     * This method checks to see if the current session in a DGAA session
     * @param request The HTTP request packet
     */
    public static void checkAuthenticationTokenDGAA(HttpServletRequest request) {
        HttpSession session = request.getSession();

        if (session.getAttribute("dgaa") != null) {
            return;
        } else {
            InsufficientPermissionException insufficientPermissionException = new InsufficientPermissionException("The user does not have permission to perform the requested action");
            logger.error(insufficientPermissionException.getMessage());
            throw insufficientPermissionException;
        }

    }

}