package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.CreateUserDTO;
import org.seng302.leftovers.dto.ModifyUserDTO;
import org.seng302.leftovers.entities.Account;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.UserNotFoundException;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.PasswordAuthenticator;
import org.seng302.leftovers.service.searchservice.SearchPageConstructor;
import org.seng302.leftovers.service.searchservice.SearchQueryParser;
import org.seng302.leftovers.service.searchservice.SearchSpecConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(UserController.class.getName());

    public UserController(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    /**
     * Check if all the details which the user has entered in the registration form are valid. If they are, add the user's
     * information to the database's account and user tables.
     * @param userinfo A Json object containing all of the user's details from the registration form.
     */
    @PostMapping("/users")
    public void register(@Valid @RequestBody CreateUserDTO userinfo, HttpServletRequest request, HttpServletResponse response) {
        logger.info("Register");
        try {
            Account.checkEmailUniqueness(userinfo.getEmail(), userRepository);

            Location homeAddress = userinfo.getHomeAddress().createLocation();

            User user = new User.Builder()
                    .withFirstName(userinfo.getFirstName())
                    .withMiddleName(userinfo.getMiddleName())
                    .withLastName(userinfo.getLastName())
                    .withNickName(userinfo.getNickname())
                    .withBio(userinfo.getBio())
                    .withAddress(homeAddress)
                    .withPhoneNumber(userinfo.getPhoneNumber())
                    .withDob(userinfo.getDateOfBirth())
                    .withEmail(userinfo.getEmail())
                    .withPassword(userinfo.getPassword())
                    .build();
            User newUser = userRepository.save(user);
            AuthenticationTokenManager.setAuthenticationToken(request, response, newUser);
            response.setStatus(201);
            logger.info("User has been registered.");

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }


    /**
     * PUT endpoint for modifying an existing user
     * User must have valid permission to make this action.
     * If the email is changed or the password has been requested to be changed, the current password must be provided.
     * @param id The ID of the user to modify
     * @param body A Json object containing all of the user's details from the modification form
     */
    @PutMapping("/users/{id}")
    public void modifyUser(@PathVariable Long id, @Valid @RequestBody ModifyUserDTO body, HttpServletRequest request) {
        logger.info("Updating user (userId={})", id);
        AuthenticationTokenManager.checkAuthenticationToken(request);
        try {
            User user = userRepository.getUser(id);

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, id)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You do not have permission to modify another user");
            }
            
            // check if email changed
            if (!body.getEmail().equals(user.getEmail())) {
                Account.checkEmailUniqueness(body.getEmail(), userRepository);
                PasswordAuthenticator.verifyPassword(body.getPassword(),user.getAuthenticationCode());
                user.setEmail(body.getEmail());
            }
            // check if password changed
            if (body.getNewPassword() != null) {
                PasswordAuthenticator.verifyPassword(body.getPassword(), user.getAuthenticationCode());
                user.setAuthenticationCodeFromPassword(body.getNewPassword());
            }

            user.setFirstName(body.getFirstName());
            user.setMiddleName(body.getMiddleName());
            user.setLastName(body.getLastName());
            user.setNickname(body.getNickname());
            user.setBio(body.getBio());
            user.setDob(body.getDateOfBirth());
            user.setPhNum(body.getPhoneNumber());
            user.setAddress(body.getHomeAddress().createLocation());

            userRepository.save(user);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * REST GET method to fetch a singular User
     * @param id The ID of the user
     * @return User with corresponding Id
     */
    @GetMapping("/users/{id}")
    public JSONObject getUserById(@PathVariable Long id, HttpServletRequest session) {
        logger.info("Get user by id");
        AuthenticationTokenManager.checkAuthenticationToken(session);

        logger.info(() -> String.format("Retrieving user with ID %d.", id));
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            UserNotFoundException notFound = new UserNotFoundException();
            logger.error(notFound.getMessage());
            throw notFound;
        } else {
            if (AuthenticationTokenManager.sessionCanSeePrivate(session, user.get().getUserID())) {
                return user.get().constructPrivateJson(true);
            } else {
                return user.get().constructPublicJson(true);
            }

        }
    }

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
    public JSONObject searchUsersByName(HttpServletRequest session,
                                @RequestParam("searchQuery") String searchQuery,
                                @RequestParam(required = false) Integer page,
                                @RequestParam(required = false) Integer resultsPerPage,
                                @RequestParam(required = false) String orderBy,
                                @RequestParam(required = false) Boolean reverse) {

        AuthenticationTokenManager.checkAuthenticationToken(session); // Check user auth

        logger.info(() -> String.format("Performing search for \"%s\"", searchQuery));
        List<User> queryResults;
        long count;
        if (orderBy == null || orderBy.equals("relevance")) {
            queryResults = SearchQueryParser.getSearchResultsOrderedByRelevance(searchQuery, userRepository, reverse);
            count = queryResults.size();
            queryResults = SearchPageConstructor.getPageInResults(queryResults, page, resultsPerPage);
        } else {
            Specification<User> spec = SearchSpecConstructor.constructUserSpecificationFromSearchQuery(searchQuery);
            Sort userSort = SearchQueryParser.getSort(orderBy, reverse);
            Page<User> results = userRepository.findAll(spec, SearchPageConstructor.getPageRequest(page, resultsPerPage, userSort));
            count = results.getTotalElements();
            queryResults = results.toList();
        }


        JSONArray resultArray = new JSONArray();
        for (User user : queryResults) {
            if (AuthenticationTokenManager.sessionCanSeePrivate(session, user.getUserID())) {
                resultArray.appendElement(user.constructPrivateJson(true));
            } else {
                resultArray.appendElement(user.constructPublicJson(true));
            }
        }
        JSONObject json = new JSONObject();
        json.put("count", count);
        json.put("results", resultArray);
        return json;
    }


    /**
     * Promotes a single user to role "Admin"
     * Only the DGAA has privilege to perform this action
     * @param session The request
     * @param id The id of the user to promote
     */
    @PutMapping("/users/{id}/makeAdmin")
    public void makeUserAdmin(HttpServletRequest session, @PathVariable("id") long id) {
        changeUserPrivilege(session, id, "globalApplicationAdmin");
    }

    /**
     * Revokes a single user from role "Admin" to role "User"
     * Only the DGAA has privilege to perform this action
     * @param session The request
     * @param id The id of the user to demote
     */
    @PutMapping("/users/{id}/revokeAdmin")
    public void revokeUserAdmin(HttpServletRequest session, @PathVariable("id") long id) {
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

        logger.info(() -> String.format("Changing user %d role to %s.", id, newRole));
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
}