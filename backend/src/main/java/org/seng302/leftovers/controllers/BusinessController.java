package org.seng302.leftovers.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.ResultPageDTO;
import org.seng302.leftovers.dto.business.BusinessResponseDTO;
import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.dto.business.CreateBusinessDTO;
import org.seng302.leftovers.dto.business.ModifyBusinessDTO;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.Image;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.exceptions.InsufficientPermissionResponseException;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.ImageRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.service.ImageService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.service.search.SearchPageConstructor;
import org.seng302.leftovers.service.search.SearchSpecConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
public class BusinessController {
    @Autowired
    private ObjectMapper objectMapper;

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private static final Logger logger = LogManager.getLogger(BusinessController.class.getName());

    private static final Set<String> VALID_BUSINESS_ORDERINGS = Set.of("created", "name", "location", "businessType");

    @Autowired
    public BusinessController(BusinessRepository businessRepository, UserRepository userRepository, ImageService imageService, ImageRepository imageRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    /**
     * POST endpoint for registering a new business.
     * Ensures that the given primary business owner is an existing User.
     * Adds the business to the database if all of the business information is valid.
     * @param body A Json object containing all of the business's details from the registration form.
     */
    @PostMapping("/businesses")
    public ResponseEntity<Void> register(@Valid @RequestBody CreateBusinessDTO body, HttpServletRequest req) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(req);
            // Make sure this is an existing user ID
            Optional<User> primaryOwner = userRepository.findById(body.getPrimaryAdministratorId());

            if (primaryOwner.isEmpty()) {
                throw new ValidationResponseException("The given PrimaryBusinessOwner does not exist");
            } else if (!AuthenticationTokenManager.sessionCanSeePrivate(req, primaryOwner.get().getUserID())) {
                throw new InsufficientPermissionResponseException("You don't have permission to set the provided Primary Owner");
            }

            // Build the business
            Business newBusiness = new Business.Builder()
                    .withPrimaryOwner(primaryOwner.get())
                    .withBusinessType(body.getBusinessType())
                    .withDescription(body.getDescription())
                    .withName(body.getName())
                    .withAddress(body.getAddress().createLocation())
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
     * PUT endpoint for modifying an existing business.
     * Ensures that the given primary business owner is an existing User.
     * Adds the business to the database if all of the business information is valid.
     * @param body A Json object containing all of the business's details from the modification form.
     */
    @PutMapping("/businesses/{id}")
    public void modifyBusiness(@PathVariable Long id, @Valid @RequestBody ModifyBusinessDTO body, HttpServletRequest request) {
        logger.info("Updating business (businessId={})", id);
        AuthenticationTokenManager.checkAuthenticationToken(request);
        try {
            Business business = businessRepository.findById(id)
                    .orElseThrow(() -> new DoesNotExistResponseException(Business.class));
            business.checkSessionPermissions(request);
            Long newAdminID = body.getPrimaryAdministratorId();
            if(!business.getPrimaryOwner().getUserID().equals(newAdminID)) {
                business.checkSessionPermissionsOwner(request);

                User newOwner = userRepository.findById(newAdminID)  
                .orElseThrow(() -> new ValidationResponseException("Updated primary administrator does not exist"));
                User previousOwner = userRepository.findById(business.getPrimaryOwner().getUserID())
                .orElseThrow(() -> new ValidationResponseException("Previous business owner account does not exist"));

                business.removeAdmin(newOwner);
                business.setPrimaryOwner(newOwner);    
                business.addAdmin(previousOwner);      
            }
            business.setName(body.getName());
            business.setDescription(body.getDescription());
            business.setAddress(body.getAddress().createLocation());
            business.setBusinessType(body.getBusinessType());

            if (Boolean.TRUE.equals(body.getUpdateProductCountry())) {
                List<Product> catalogue = business.getCatalogue();
                String countryToChange = body.getAddress().getCountry();
                // Iterate through each product in the catalogue and change their country to the specified country
                for (Product product : catalogue) {
                    product.setCountryOfSale(countryToChange);
                }
            }

            businessRepository.save(business);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * This method searchs for the business with the given ID in the database. If the business exists, return a JSON representation of the
     * business. If the business does not exists, send a response with status code 406/Not Acceptable.
     * @return JSON representation of the business.
     */
    @GetMapping("/businesses/{id}")
    public BusinessResponseDTO getBusinessById(@PathVariable Long id, HttpServletRequest request) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        logger.info("Retrieving business with ID {}.", id);
        Optional<Business> business = businessRepository.findById(id);
        if (business.isEmpty()) {
            var notFoundException = new DoesNotExistResponseException(Business.class);
            logger.error(notFoundException.getMessage());
            throw notFoundException;
        }
        return BusinessResponseDTO.withAdmins(business.get());
    }

    /**
     * DTO for getting the ID number of the user that is being promoted/demoted to a GAA.
     */
    @Getter
    private static class PromoteDemoteDTO {
        @NotNull
        private Long userId;
    }


    /**
     * PUT endpoint for making an individual an administrator of a business
     * The business primary owner and system administrator can do this
     * @param userInfo The info containing the UserId for the User to make an administrator
     * @param businessId The Id of the business
     */
    @PutMapping("/businesses/{id}/makeAdministrator")
    public void makeAdmin(@RequestBody @Valid PromoteDemoteDTO userInfo, HttpServletRequest req, @PathVariable("id") Long businessId) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(req); // Ensure a user is logged in
            Business business = getBusiness(businessId); // Get the business
            business.checkSessionPermissionsOwner(req);

            User user = userRepository.findById(userInfo.getUserId())
                    .orElseThrow(() -> new ValidationResponseException("The given user does not exist"));
            LocalDate now = LocalDate.now();
            LocalDate minDate = now.minusYears(16);
            
            if (user.getDob().compareTo(minDate) < 0) {
                business.addAdmin(user);
                businessRepository.save(business);
                logger.info(() -> String.format("Added user %d as admin of business %d", user.getUserID(), businessId));
            } else {
                throw new ValidationResponseException("The new business admin should be at least 16 years old");
            }
        } catch (Exception err) {
            logger.error(err.getMessage());
            throw err;
        }
    }

    /**
     * PUT endpoint for removing the administrator status of a user from a business
     * The business primary owner and system administrator can do this
     * @param userInfo The info containing the UserId for the User to remove from the list of administrators
     * @param businessId The Id of the business
     */
    @PutMapping("/businesses/{id}/removeAdministrator")
    public void removeAdmin(@RequestBody @Valid PromoteDemoteDTO userInfo, HttpServletRequest req, @PathVariable("id") Long businessId) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(req); // Ensure a user is logged in
            Business business = getBusiness(businessId); // Get the business
            business.checkSessionPermissionsOwner(req);
            User user = userRepository.findById(userInfo.getUserId())
                    .orElseThrow(() -> new ValidationResponseException("The given user does not exist"));

            business.removeAdmin(user);
            businessRepository.save(business);
            logger.info(() -> String.format("Removed user %d as admin of business %d", user.getUserID(), businessId));
        } catch (Exception err) {
            logger.error(err.getMessage());
            throw err;
        }

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
        if (business.isEmpty()) {
            throw new DoesNotExistResponseException(Business.class);
        }
        return business.get();
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
     * @param businessTypeString Type of business. Can by one of "Accommodation and Food Services", "Retail Trade","Charitable organisation", "Non-profit organisation".
     * @return A JSON object containing the total count and paginated results.
     */
    @GetMapping("/businesses/search")
    public ResultPageDTO<BusinessResponseDTO> search(HttpServletRequest request, @RequestParam(required = false) String searchQuery,
                                @RequestParam(required = false) Integer page,
                                @RequestParam(required = false) Integer resultsPerPage,
                                @RequestParam(required = false) String orderBy,
                                @RequestParam(required = false) Boolean reverse,
                                @RequestParam(required = false, name = "businessType") String businessTypeString) {

        AuthenticationTokenManager.checkAuthenticationToken(request);

        logger.info("Performing Business search for query \"{}\" and type \"{}\"", searchQuery, businessTypeString);


        BusinessType businessType;
        try {
            businessType = objectMapper.convertValue(businessTypeString, new TypeReference<>() {});
        } catch (IllegalArgumentException e) {
            throw new ValidationResponseException("Invalid business type provided");
        }

        Sort.Direction direction = SearchPageConstructor.getSortDirection(reverse);
        if (orderBy == null) {
            orderBy = "created";
        }
        if (!VALID_BUSINESS_ORDERINGS.contains(orderBy)) {
            logger.error("Invalid 'orderBy' parameter {} used", orderBy);
            throw new ValidationResponseException("Invalid business ordering");
        }

        List<Sort.Order> sortOrder;
        if (orderBy.equals("location")) {
            sortOrder = List.of(new Sort.Order(direction, "address.country").ignoreCase(), new Sort.Order(direction, "address.city").ignoreCase());
        } else {
            sortOrder = List.of(new Sort.Order(direction, orderBy).ignoreCase());
        }

        PageRequest pageRequest = SearchPageConstructor.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));
        Specification<Business> specification = SearchSpecConstructor.constructSpecificationFromBusinessSearch(searchQuery, businessType);

        Page<Business> results = businessRepository.findAll(specification, pageRequest);
        return new ResultPageDTO<>(results.map(BusinessResponseDTO::withoutAdmins));
    }

    /**
     * Adds a new image to the business' image list
     * @param businessId Identifier of business to add image to
     * @param file Uploaded image
     * @return Empty response with 201 if successful
     */
    @PostMapping("/businesses/{businessId}/images")
    public ResponseEntity<Void> uploadImage(@PathVariable Long businessId, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        logger.info("Adding business image (businessId={})", businessId);
        AuthenticationTokenManager.checkAuthenticationToken(request);
        try {
            Business business = businessRepository.getBusinessById(businessId);
            business.checkSessionPermissions(request);

            Image image = imageService.create(file);
            business.addImage(image);
            businessRepository.save(business);

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Sets the new primary image to be displayed for a business
     * @param businessId The ID of the business to modify
     * @param imageId The ID of the image to set as primary image
     * @return Empty response with 200 if successful
     */
    @PutMapping("/businesses/{businessId}/images/{imageId}/makeprimary")
    public ResponseEntity<Void> makeImagePrimary(@PathVariable Long businessId, @PathVariable Long imageId, HttpServletRequest request) {
        logger.info("Making business image primary (businessId={}, imageId={})", businessId, imageId);
        AuthenticationTokenManager.checkAuthenticationToken(request);
        try {
            Business business = businessRepository.getBusinessById(businessId);
            business.checkSessionPermissions(request);

            Image image = imageRepository.getImageById(imageId);
            var images = business.getImages();
            // Ensure that the provided image belongs to this business. Otherwise, action is forbidden
            if (!images.contains(image)) {
                throw new InsufficientPermissionResponseException("You cannot modify this image");
            }
            if (images.get(0).equals(image)) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            business.removeImage(image);
            business.addImage(0, image);
            businessRepository.save(business);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
}
