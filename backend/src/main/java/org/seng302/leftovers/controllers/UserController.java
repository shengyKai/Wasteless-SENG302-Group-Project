package org.seng302.leftovers.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.ResultPageDTO;
import org.seng302.leftovers.dto.user.CreateUserDTO;
import org.seng302.leftovers.dto.user.ModifyUserDTO;
import org.seng302.leftovers.dto.user.UserResponseDTO;
import org.seng302.leftovers.dto.user.UserRole;
import org.seng302.leftovers.entities.Account;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.exceptions.InsufficientPermissionResponseException;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.service.search.SearchPageConstructor;
import org.seng302.leftovers.service.search.SearchQueryParser;
import org.seng302.leftovers.service.search.SearchSpecConstructor;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.PasswordAuthenticator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {
    private static final List<String> USER_ORDER_BY_OPTIONS = List.of("userID", "firstName", "middleName", "lastName", "nickname", "email");
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private static final Logger logger = LogManager.getLogger(UserController.class.getName());

    public UserController(UserRepository userRepository, ImageRepository imageRepository) {

        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    /**
     * This method constructs a Sort object to be passed into a query for searching the UserRepository. The attribute
     * which Users should be sorted by and whether that order should be reversed are specified.
     * @param orderBy The attribute which query results will be ordered by.
     * @param reverse Results will be in descending order if true, ascending order if false or null.
     * @return A Sort which can then be applied to queries of the UserRepository.
     */
    public static Sort getSort(String orderBy, Boolean reverse) {
        if (orderBy == null || !USER_ORDER_BY_OPTIONS.contains(orderBy)) {
            orderBy = "userID";
        }

        return Sort.by(SearchPageConstructor.getSortDirection(reverse), orderBy);
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
                throw new InsufficientPermissionResponseException(
                        "You do not have permission to modify another user");
            }
            if(user.getRole().equals(UserRole.DGAA)) {
                throw new InsufficientPermissionResponseException(
                        "DGAA profile are not permitted to be modified.");
            }
            if (!body.getEmail().equals(user.getEmail())) {
                Account.checkEmailUniqueness(body.getEmail(), userRepository);
                PasswordAuthenticator.verifyPassword(body.getPassword(),user.getAuthenticationCode());
                user.setEmail(body.getEmail());
            }
            if (body.getNewPassword() != null) {
                PasswordAuthenticator.verifyPassword(body.getPassword(), user.getAuthenticationCode());
                user.setAuthenticationCodeFromPassword(body.getNewPassword());
            }

            user.setImages(imageRepository.getImagesByIds(body.getImageIds()));

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
    public UserResponseDTO getUserById(@PathVariable Long id, HttpServletRequest session) {
        logger.info("Get user by id");
        AuthenticationTokenManager.checkAuthenticationToken(session);

        logger.info(() -> String.format("Retrieving user with ID %d.", id));
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            var notFound = new DoesNotExistResponseException(User.class);
            logger.error(notFound.getMessage());
            throw notFound;
        } else {
            return new UserResponseDTO(
                    user.get(),
                    true,
                    AuthenticationTokenManager.sessionCanSeePrivate(session, user.get().getUserID())
            );
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
    public ResultPageDTO<UserResponseDTO> searchUsersByName(HttpServletRequest session,
                                                            @RequestParam("searchQuery") String searchQuery,
                                                            @RequestParam(required = false) Integer page,
                                                            @RequestParam(required = false) Integer resultsPerPage,
                                                            @RequestParam(required = false) String orderBy,
                                                            @RequestParam(required = false) Boolean reverse) {

        AuthenticationTokenManager.checkAuthenticationToken(session); // Check user auth

        logger.info(() -> String.format("Performing search for \"%s\"", searchQuery));
        Page<User> results;
        if (orderBy == null || orderBy.equals("relevance")) {
            var users = SearchQueryParser.getSearchResultsOrderedByRelevance(searchQuery, userRepository, reverse);
            results = new PageImpl<>(SearchPageConstructor.getPageInResults(users, page, resultsPerPage), Pageable.unpaged(), users.size());
        } else {
            Specification<User> spec = SearchSpecConstructor.constructUserSpecificationFromSearchQuery(searchQuery);
            Sort userSort = getSort(orderBy, reverse);
            results = userRepository.findAll(spec, SearchPageConstructor.getPageRequest(page, resultsPerPage, userSort));
        }

        return new ResultPageDTO<>(
                results.map(user -> new UserResponseDTO(
                        user,
                        true,
                        AuthenticationTokenManager.sessionCanSeePrivate(session, user.getUserID()))
                )
        );
    }



    /**
     * Promotes a single user to role "Admin"
     * Only the DGAA has privilege to perform this action
     * @param session The request
     * @param id The id of the user to promote
     */
    @PutMapping("/users/{id}/makeAdmin")
    public void makeUserAdmin(HttpServletRequest session, @PathVariable("id") long id) {
        changeUserPrivilege(session, id, UserRole.GAA);
    }

    /**
     * Revokes a single user from role "Admin" to role "User"
     * Only the DGAA has privilege to perform this action
     * @param session The request
     * @param id The id of the user to demote
     */
    @PutMapping("/users/{id}/revokeAdmin")
    public void revokeUserAdmin(HttpServletRequest session, @PathVariable("id") long id) {
        changeUserPrivilege(session, id, UserRole.USER);
    }

    /**
     * Changes the role of a user
     * @param request The HTTP Request
     * @param id The id of the user
     * @param newRole The new role of the user
     */
    void changeUserPrivilege(HttpServletRequest request, long id, UserRole newRole) {
        AuthenticationTokenManager.checkAuthenticationToken(request); // Ensure user is logged on
        AuthenticationTokenManager.checkAuthenticationTokenDGAA(request); // Ensure user is the DGAA

        logger.info(() -> String.format("Changing user %d role to %s.", id, newRole));
        long userId = id;
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            var notFoundException = new DoesNotExistResponseException(User.class);
            logger.error(notFoundException.getMessage());
            throw notFoundException;
        } else {
            user.get().setRole(newRole);
            userRepository.save(user.get());
        }
    }


}