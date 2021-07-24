package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
public class BusinessController {
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(BusinessController.class.getName());

    private static final Set<String> VALID_BUSINESS_ORDERINGS = Set.of("created", "name", "location", "businessType");


    public BusinessController(BusinessRepository businessRepository, UserRepository userRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
    }

    /**
     * Parses the address part of the business info and constructs a location object using the Location class function
     * @param businessInfo Business info
     * @return A new Location object containing the business address
     */
    private Location parseLocation(JSONObject businessInfo) {
        JSONObject businessLocation = new JSONObject((Map<String, ?>) businessInfo.get("address")) ;
        return Location.parseLocationFromJson(businessLocation);
    }

    /**
     * Check that the JSON body for the POST endpoint is present and has all the required fields.
     * @param businessInfo The JSON body of the request sent to the POST businesses endpoint.
     */
    private void checkRegisterJson(JSONObject businessInfo) {
        if (businessInfo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request must contain a JSON body");
        }
        if (businessInfo.get("primaryAdministratorId") == null ||
            businessInfo.get("name") == null ||
            businessInfo.get("description") == null ||
            businessInfo.get("businessType") == null ||
            businessInfo.get("address") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body must contain the fields " +
            "\"primaryAdministratorId\", \"name\", \"description\" and \"businessType\"");
        }
    }

    /**
     * POST endpoint for registering a new business.
     * Ensures that the given primary business owner is an existing User.
     * Adds the business to the database if all of the business information is valid.
     * @param businessInfo A Json object containing all of the business's details from the registration form.
     */
    @PostMapping("/businesses")
    public ResponseEntity<Void> register(@RequestBody JSONObject businessInfo, HttpServletRequest req) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(req);
            // Make sure this is an existing user ID
            Optional<User> primaryOwner = userRepository.findById(Long.parseLong((businessInfo.getAsString("primaryAdministratorId"))));

            if (primaryOwner.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The given PrimaryBusinessOwner does not exist");
            } else if (!AuthenticationTokenManager.sessionCanSeePrivate(req, primaryOwner.get().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You don't have permission to set the provided Primary Owner");
            }

            checkRegisterJson(businessInfo);
            Location address = parseLocation(businessInfo); // Get the address for the business
            // Build the business
            Business newBusiness = new Business.Builder()
                    .withPrimaryOwner(primaryOwner.get())
                    .withBusinessType(businessInfo.getAsString("businessType"))
                    .withDescription(businessInfo.getAsString("description"))
                    .withName(businessInfo.getAsString("name"))
                    .withAddress(address)
                    .build();

            businessRepository.save(newBusiness); // Save the new business
            logger.info("Business has been registered");
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (Exception err) {
            logger.error(err.getMessage());
            throw err;
        }
    }

    /**
     * This method searchs for the business with the given ID in the database. If the business exists, return a JSON representation of the
     * business. If the business does not exists, send a response with status code 406/Not Acceptable.
     * @return JSON representation of the business.
     */
    @GetMapping("/businesses/{id}")
    public JSONObject getBusinessById(@PathVariable Long id, HttpServletRequest request) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        logger.info(() -> String.format("Retrieving business with ID %d.", id));
        Optional<Business> business = businessRepository.findById(id);
        if (business.isEmpty()) {
            ResponseStatusException notFoundException = new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format("No business with ID %d.", id));
            logger.error(notFoundException.getMessage());
            throw notFoundException;
        }
        return business.get().constructJson(true);
    }


    /**
     * PUT endpoint for making an individual an administrator of a business
     * Only the business primary owner can do this
     * @param userInfo The info containing the UserId for the User to make an administrator
     * @param businessId The Id of the business
     */
    @PutMapping("/businesses/{id}/makeAdministrator")
    public void makeAdmin(@RequestBody JSONObject userInfo, HttpServletRequest req, @PathVariable("id") Long businessId) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(req); // Ensure a user is logged in
            Business business = getBusiness(businessId); // Get the business
            loggedInUserHasPermissions(req, business);
            User user = getUser(userInfo); // Get the user to promote

            business.addAdmin(user);
            businessRepository.save(business);
            logger.info(() -> String.format("Added user %d as admin of business %d", user.getUserID(), businessId));
        } catch (Exception err) {
            logger.error(err.getMessage());
            throw err;
        }
    }

    /**
     * PUT endpoint for removing the administrator status of a user from a business
     * Only the business primary owner can do this
     * @param userInfo The info containing the UserId for the User to remove from the list of administrators
     * @param businessId The Id of the business
     */
    @PutMapping("/businesses/{id}/removeAdministrator")
    public void removeAdmin(@RequestBody JSONObject userInfo, HttpServletRequest req, @PathVariable("id") Long businessId) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(req); // Ensure a user is logged in
            Business business = getBusiness(businessId); // Get the business
            loggedInUserHasPermissions(req, business);
            User user = getUser(userInfo); // Get the user to demote

            business.removeAdmin(user);
            businessRepository.save(business);
            logger.info(() -> String.format("Removed user %d as admin of business %d", user.getUserID(), businessId));
        } catch (Exception err) {
            logger.error(err.getMessage());
            throw err;
        }

    }

    /**
     * Gets a user from the database and performs sanity checks to ensure User is not null
     * Throws a ResponseStatusException if the user does not exist
     * @param userInfo Data containing the Id of the user to find
     * @return A user of given UserId
     */
    private User getUser(@RequestBody JSONObject userInfo) {
        // Check a valid Long id is given in the request
        if (!userInfo.containsKey("userId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not find a user id in the request");
        }

        Long userId = userInfo.getAsNumber("userId").longValue();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not find a user id in the request");
        }
        // check the requested user exists
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The given user does not exist");
        }
        return user.get();
    }

    /**
     * Gets a business from the database matching a given Business Id
     * Performs sanity checks to ensure the business is not null
     * Throws ResponseStatusException if business does not exist
     * @param businessId The id of the business to retrieve
     * @return The business matching the given Id
     */
    private Business getBusiness(Long businessId) {
        // check business exists
        Optional<Business> business = businessRepository.findById(businessId);
        if (!business.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    "The given business does not exist");
        }
        return business.get();
    }

    /**
     * Determines if the currently logged in user is the Primary Owner of the given business
     * @param req The httpRequest
     * @param business The business to compare
     * @return User is Primary owner
     */
    private boolean loggedInUserIsOwner(HttpServletRequest req, Business business) {
        HttpSession session = req.getSession();
        Long userId = (Long) session.getAttribute("accountId");
        return userId != null && userId.equals(business.getPrimaryOwner().getUserID());
    }

    /**
     * Determines if the currently logged in user is Primary Owner OR an application admin
     * Throws a ResponseStatusException if they are neither Primary Owner OR an application admin
     * @param req The httpRequest
     * @param business The business to compare
     */
    private void loggedInUserHasPermissions(HttpServletRequest req, Business business) {
        // check user is owner
        if (!(loggedInUserIsOwner(req, business) || AuthenticationTokenManager.sessionCanSeePrivate(req, null))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to perform this action");
        }
    }


    /**
     * Searches for businesses matching a search query and/or business type. Results are paginated
     * The query string can contain AND and OR operators to refine the search.
     * Searching performs partial matches by default. Using quotation marks performs exact matches.
     * @param request The HTTP Request
     * @param searchQuery The search term
     * @param page Page number to display
     * @param resultsPerPage Number of results per page
     * @param orderBy Order by term. Can be one of "created", "name", "location", "businessType"
     * @param reverse Boolean. Reverse ordering of results
     * @param businessType Type of business. Can by one of "Accommodation and Food Services", "Retail Trade",
     *                     "Charitable organisation", "Non-profit organisation".
     * @return A JSON object containing the total count and paginated results.
     */
    @GetMapping("/businesses/search")
    public JSONObject search(HttpServletRequest request, @RequestParam(required = false) String searchQuery,
                             @RequestParam(required = false) Integer page,
                             @RequestParam(required = false) Integer resultsPerPage,
                             @RequestParam(required = false) String orderBy,
                             @RequestParam(required = false) Boolean reverse,
                             @RequestParam(required = false) String businessType) {

        AuthenticationTokenManager.checkAuthenticationToken(request);

        logger.info(() -> String.format("Performing Business search for query \"%s\" and type \"%s\"", searchQuery, businessType));

        Sort.Direction direction = SearchHelper.getSortDirection(reverse);
        if (orderBy == null) {
            orderBy = "created";
        }
        if (!VALID_BUSINESS_ORDERINGS.contains(orderBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid business ordering");
        }

        List<Sort.Order> sortOrder;
        if (orderBy.equals("location")) {
            sortOrder = List.of(new Sort.Order(direction, "address.country").ignoreCase(), new Sort.Order(direction, "address.city").ignoreCase());
        } else {
            sortOrder = List.of(new Sort.Order(direction, orderBy).ignoreCase());
        }

        PageRequest pageRequest = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));
        Specification<Business> specification = SearchHelper.constructSpecificationFromBusinessSearch(searchQuery, businessType);

        Page<Business> results = businessRepository.findAll(specification, pageRequest);
        Long count = results.getTotalElements();

        JSONArray resultArray = new JSONArray();
        for (Business business : results) {
            resultArray.appendElement(business.constructJson(AuthenticationTokenManager.sessionIsAdmin(request)));
        }
        JSONObject json = new JSONObject();
        json.put("count", count);
        json.put("results", resultArray);
        return json;
    }
}
