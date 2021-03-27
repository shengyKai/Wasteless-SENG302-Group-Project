package org.seng302.Controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.Entities.Location;
import org.seng302.Entities.User;
import org.seng302.Exceptions.EmailInUseException;
import org.seng302.Exceptions.FailedRegisterException;
import org.seng302.Exceptions.UserNotFoundException;
import org.seng302.Persistence.UserRepository;
import org.seng302.Tools.AuthenticationTokenManager;
import org.seng302.Tools.UserSearchHelper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

@RestController
public class UsersController {




    private final UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(UsersController.class.getName());

    public UsersController(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    /**
     * Check if all the details which the user has entered in the registration form are valid. If they are, add the user's
     * information to the database's account and user tables.
     * @param userinfo A Json object containing all of the user's details from the registration form.
     */
    @PostMapping("/users")
    public void register(@RequestBody JSONObject userinfo, HttpServletRequest request, HttpServletResponse response) {
        try {
            User.checkEmailUniqueness(userinfo.getAsString("email"), userRepository);
        } catch (EmailInUseException inUseException) {
            logger.error(inUseException.getMessage());
            throw inUseException;
        }
        try {
            User user = new User.Builder()
                    .withFirstName(userinfo.getAsString("firstName"))
                    .withMiddleName(userinfo.getAsString("middleName"))
                    .withLastName(userinfo.getAsString("lastName"))
                    .withNickName(userinfo.getAsString("nickname"))
                    .withBio(userinfo.getAsString("bio"))
                    .withAddress(Location.covertAddressStringToLocation(userinfo.getAsString("homeAddress")))
                    .withPhoneNumber(userinfo.getAsString("phoneNumber"))
                    .withDob(userinfo.getAsString("dateOfBirth"))
                    .withEmail(userinfo.getAsString("email"))
                    .withPassword(userinfo.getAsString("password"))
                    .build();
            userRepository.save(user);
            AuthenticationTokenManager.setAuthenticationToken(request, response);
            response.setStatus(201);
            logger.info("User has been registered.");
        } catch (ResponseStatusException responseError) {
            logger.error(responseError.getMessage());
            throw responseError;
        } catch (ParseException e) {
            e.printStackTrace();
            throw new FailedRegisterException("Could not process date of birth.");
        }

    }

    /**
     * Parses the user object into the SQLite database
     */
    public void parseUserIntoDatabase(User user) {

    }



    /**
     * REST GET method to fetch a singular User
     * @param id The ID of the user
     * @return User with corresponding Id
     */
    @GetMapping("/users/{id}")
    JSONObject getUserById(@PathVariable Long id, HttpServletRequest session) {
        AuthenticationTokenManager.checkAuthenticationToken(session);

        logger.info(String.format("Retrieving user with ID %d.", id));
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            UserNotFoundException notFound = new UserNotFoundException();
            logger.error(notFound.getMessage());
            throw notFound;
        } else {
            return user.get().constructPublicJson();
        }
    };

    /**
     * REST GET method to search for users matching a search query
     * @param searchQuery The search term
     * @param page The page number in the results to be returned (defaults to one)
     * @param resultsPerPage The number of results that should be in the returned list (defaults to 15).
     * @param orderBy The name of the attribute to order the search results by.
     * @param reverse String representation of boolean indicating whether results should be in reverse order.
     * @return List of matching Users
     */
    @GetMapping("/users/search")
    JSONArray searchUsersByName(HttpServletRequest session,
                                @RequestParam("searchQuery") String searchQuery,
                                @RequestParam(required = false) String page,
                                @RequestParam(required = false) String resultsPerPage,
                                @RequestParam(required = false) String orderBy,
                                @RequestParam(required = false) String reverse) {

        AuthenticationTokenManager.checkAuthenticationToken(session); // Check user auth

        logger.info(String.format("Performing search for \"%s\"", searchQuery));
        List<User> queryResults;
        if (orderBy == null || orderBy.equals("relevance")) {
            queryResults = UserSearchHelper.getSearchResultsOrderedByRelevance(searchQuery, userRepository);
        } else {
            Specification<User> spec = UserSearchHelper.constructUserSpecificationFromSearchQuery(searchQuery);
            Sort userSort = UserSearchHelper.getSort(orderBy, reverse);
            queryResults = userRepository.findAll(spec, userSort);
        }
        List<User> pageInResults = UserSearchHelper.getPageInResults(queryResults, page, resultsPerPage);
        JSONArray publicResults = new JSONArray();
        for (User user : pageInResults) {
            publicResults.appendElement(user.constructPublicJson());
        }
        return publicResults;
    }


    /**
     * Promotes a single user to role "Admin"
     * Only the DGAA has privilege to perform this action
     * @param session The request
     * @param id The id of the user to promote
     */
    @GetMapping("/users/{id}/makeAdmin")
    void makeUserAdmin(HttpServletRequest session, @PathVariable("id") long id) {
        changeUserPrivilege(session, id, "admin");
    }

    /**
     * Revokes a single user from role "Admin" to role "User"
     * Only the DGAA has privilege to perform this action
     * @param session The request
     * @param id The id of the user to demote
     */
    @GetMapping("/users/{id}/revokeAdmin")
    void revokeUserAdmin(HttpServletRequest session, @PathVariable("id") long id) {
        changeUserPrivilege(session, id, "user");
    }

    /**
     * Changes the role of a user
     * @param request The HTTP Request
     * @param id The id of the user
     * @param newRole The new role of the user
     */
    void changeUserPrivilege(HttpServletRequest request, long id, String newRole) {
        AuthenticationTokenManager.checkAuthenticationToken(request); // Ensure user is logged on
        AuthenticationTokenManager.checkAuthenticationTokenDGAA(request); // Ensure user is the DGAA

        logger.info(String.format("Changing user %d role to %s.", id, newRole));
        long userId = id;
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            UserNotFoundException notFoundException = new UserNotFoundException("The requested user does not exist");
            logger.error(notFoundException.getMessage());
            throw notFoundException;
        } else {
            user.get().setRole(newRole);
            userRepository.save(user.get());
        }
    }

    /**
     * For development
     * Starts a session
     * @param request
     * @param response
     */
    @GetMapping("/dev/session")
    void experimentalGetSession(HttpServletRequest request, HttpServletResponse response) {
        AuthenticationTokenManager.setAuthenticationToken(request, response);
        AuthenticationTokenManager.setAuthenticationTokenDGAA(request);
    }
}