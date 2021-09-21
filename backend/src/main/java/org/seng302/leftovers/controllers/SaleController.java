package org.seng302.leftovers.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.SaleListingSearchDTO;
import org.seng302.leftovers.dto.ResultPageDTO;
import org.seng302.leftovers.dto.saleitem.CreateSaleItemDTO;
import org.seng302.leftovers.dto.saleitem.SaleItemResponseDTO;
import org.seng302.leftovers.dto.saleitem.SetSaleItemInterestDTO;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.entities.event.InterestEvent;
import org.seng302.leftovers.entities.event.PurchasedEvent;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.exceptions.InsufficientPermissionResponseException;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.persistence.*;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.seng302.leftovers.persistence.event.InterestEventRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.service.searchservice.SearchPageConstructor;
import org.seng302.leftovers.service.searchservice.SearchQueryParser;
import org.seng302.leftovers.service.searchservice.SearchSpecConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

@RestController
public class SaleController {
    private static final Logger logger = LogManager.getLogger(SaleController.class);

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final SaleItemRepository saleItemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InterestEventRepository interestEventRepository;
    private final BoughtSaleItemRepository boughtSaleItemRepository;
    private final EventRepository eventRepository;

    public SaleController(UserRepository userRepository, BusinessRepository businessRepository,
                          SaleItemRepository saleItemRepository, InventoryItemRepository inventoryItemRepository,
                          InterestEventRepository interestEventRepository, BoughtSaleItemRepository boughtSaleItemRepository,
                          EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.saleItemRepository = saleItemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.interestEventRepository = interestEventRepository;
        this.boughtSaleItemRepository = boughtSaleItemRepository;
        this.eventRepository = eventRepository;
    }

    private static final Set<String> VALID_SEARCH_ORDERINGS = Set.of("created", "closing", "productName", "quantity", "price", "businessName", "businessLocation");
    private static final Set<String> VALID_BUSINESS_TYPES = Set.of("Accommodation and Food Services", "Retail Trade", "Charitable organisation", "Non-profit organisation");

    /**
     * Returns the ordering term for the Sort object for sorting Sale Items
     *
     * @param orderBy   Order by term
     * @param direction Ascending or descending
     * @return Order term of Sort
     */
    private Sort.Order getSaleItemOrder(String orderBy, Sort.Direction direction) {
        if (orderBy == null) orderBy = "created";
        switch (orderBy) {
            case "created": case "quantity": case "price":
                break;
            case "productName":
                orderBy = "inventoryItem.product.name";
                break;
            case "closing":
                orderBy = "closes";
                break;
            default:
                logger.error("Invalid sale item ordering given: {}", orderBy);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided ordering is invalid");
        }
        return new Sort.Order(direction, orderBy).ignoreCase();
    }

    /**
     * DTO representing the response from adding a sale item
     */
    @Getter
    @ToString
    @AllArgsConstructor
    public static class CreateSaleItemResponseDTO {
        private Long listingId;
    }

    /**
     * Parse search order term into proper access of term to sort by
     * @param orderBy term to order results by in sale search
     * @param direction Sort.Order value for Ascending or descending
     * @return
     */
    private List<Sort.Order> getSaleItemSearchOrder(String orderBy, Sort.Direction direction) {
        if (orderBy == null) orderBy = "created";
        switch (orderBy) {
            case "created": case "quantity": case "price":
                return List.of(new Sort.Order(direction, orderBy).ignoreCase());
            case "productName":
                return List.of(new Sort.Order(direction, "inventoryItem.product.name").ignoreCase());
            case "closing":
                return List.of(new Sort.Order(direction, "closes").ignoreCase());
            case "businessName":
                return List.of(new Sort.Order(direction, "inventoryItem.product.business.name").ignoreCase());
            case "businessLocation":
                return List.of(new Sort.Order(direction, "inventoryItem.product.business.address.country"), new Sort.Order(direction, "inventoryItem.product.business.address.city"));
            default:
                logger.error("Invalid sale item ordering given: {}", orderBy);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided ordering is invalid");
        }
    }

    /**
     * Adds a sale item to a given business
     *
     * @param id           The id of the business to add to
     * @param saleItemInfo The content of the sale item
     * @return The ID of the new listing
     */
    @PostMapping("/businesses/{id}/listings")
    public CreateSaleItemResponseDTO addSaleItemToBusiness(@PathVariable Long id, @RequestBody @Valid CreateSaleItemDTO saleItemInfo, HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info("Adding sales item to business (businessId={}).", id);
            Business business = businessRepository.getBusinessById(id);
            business.checkSessionPermissions(request);

            InventoryItem inventoryItem = inventoryItemRepository.findInventoryItemByBusinessAndId(business,saleItemInfo.getInventoryItemId())
                    .orElseThrow(() -> new ValidationResponseException("Inventory item does not exist for this business"));

            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(inventoryItem)
                    .withQuantity(saleItemInfo.getQuantity())
                    .withPrice(saleItemInfo.getPrice())
                    .withMoreInfo(saleItemInfo.getMoreInfo())
                    .withCloses(saleItemInfo.getCloses())
                    .build();
            saleItem = saleItemRepository.save(saleItem);

            response.setStatus(201);
            return new CreateSaleItemResponseDTO(saleItem.getId());
        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }

    /**
     * REST GET method to retrieve all the sale items for a given business
     *
     * @param id      the id of the business
     * @param request the HTTP request
     * @return List of sale items the business is listing
     */
    @GetMapping("/businesses/{id}/listings")
    public ResultPageDTO<SaleItemDTO> getSaleItemsForBusiness(@PathVariable Long id,
                                                                      HttpServletRequest request,
                                                                      @RequestParam(required = false) String orderBy,
                                                                      @RequestParam(required = false) Integer page,
                                                                      @RequestParam(required = false) Integer resultsPerPage,
                                                                      @RequestParam(required = false) Boolean reverse) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info("Getting sales item for business (businessId={}).", id);
            Business business = businessRepository.getBusinessById(id);

            Sort.Direction direction = SearchQueryParser.getSortDirection(reverse);
            List<Sort.Order> sortOrder = List.of(getSaleItemOrder(orderBy, direction));

            PageRequest pageRequest = SearchPageConstructor.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));

            Specification<SaleItem> specification = SearchSpecConstructor.constructSpecificationFromSaleItemsFilter(business);
            Page<SaleItem> result = saleItemRepository.findAll(specification, pageRequest);

            return new ResultPageDTO<>(result.map(SaleItemDTO::new));

        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }

    /**
     * REST GET method to return sale items that match search criteria
     * @param basicSearchQuery string to match any field in basic search
     * @param productSearchQuery string to match to product name or description
     * @param businessSearchQuery string to match to business name
     * @param locationSearchQuery string to match to business location
     * @param page page to return
     * @param resultsPerPage amount of results to put on each page
     * @param reverse highest to lowest or lowest to highest
     * @param orderBy field to order results by
     * @param businessTypes list of types of business to restrict results to
     * @param priceLower all products must be above this price
     * @param priceUpper all products must be below this price
     * @param closeLower all products must close after this date
     * @param closeUpper all products must close before this date
     * @return JSON page of sale items
     */
    @GetMapping("/businesses/listings/search")
    public ResultPageDTO<SaleItemDTO> searchSaleItems(HttpServletRequest request,
                                                      @RequestParam(required = false) String basicSearchQuery,
                                                      @RequestParam(required = false) String productSearchQuery,
                                                      @RequestParam(required = false) String businessSearchQuery,
                                                      @RequestParam(required = false) String locationSearchQuery,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer resultsPerPage,
                                                      @RequestParam(required = false) Boolean reverse,
                                                      @RequestParam(required = false) String orderBy,
                                                      @RequestParam(required = false) List<String> businessTypes,
                                                      @RequestParam(required = false) String priceLower,
                                                      @RequestParam(required = false) String priceUpper,
                                                      @RequestParam(required = false) String closeLower,
                                                      @RequestParam(required = false) String closeUpper) {
        try {
            // Check auth
            logger.info("Get sale items to match parameters.");
            AuthenticationTokenManager.checkAuthenticationToken(request);

            // Check sort ordering
            if (orderBy == null || orderBy.isEmpty()) {
                orderBy = "productName";
            }
            Sort.Direction direction = SearchQueryParser.getSortDirection(reverse);
            List<Sort.Order> sortOrder = getSaleItemSearchOrder(orderBy, direction);

            // Check filter options
            if (businessTypes != null) {
                for (String businessType : businessTypes) {
                    if (!VALID_BUSINESS_TYPES.contains(businessType)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BusinessType term " + businessType + " is invalid");
                    }
                }
            }
            BigDecimal minPrice = null;
            BigDecimal maxPrice = null;
            if (priceLower != null && !priceLower.isEmpty()) minPrice = new BigDecimal(priceLower);
            if (priceUpper != null && !priceUpper.isEmpty()) maxPrice = new BigDecimal(priceUpper);

            LocalDate minDate = null;
            LocalDate maxDate = null;
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            if (closeLower != null && !closeLower.isEmpty()) minDate = LocalDate.parse(closeLower, formatter);
            if (closeUpper != null && !closeUpper.isEmpty()) maxDate = LocalDate.parse(closeUpper, formatter);

            // Create page
            PageRequest pageablePage = SearchPageConstructor.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));
            Specification<SaleItem> specification = Specification.where(
                    SearchSpecConstructor.constructSaleItemSpecificationFromSearchQueries(basicSearchQuery, productSearchQuery, businessSearchQuery, locationSearchQuery))
                            .and(SearchSpecConstructor.constructSaleListingSpecificationForSearch(new SaleListingSearchDTO(minPrice, maxPrice, minDate, maxDate, businessTypes)));
            Page<SaleItem> result = saleItemRepository.findAll(specification, pageablePage);

            return new ResultPageDTO<>(result.map(SaleItemDTO::new));

        } catch (DateTimeParseException badDate) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Close date parameters were not in date format");
        } catch (NumberFormatException badPrice) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price parameters were not valid numbers");
        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }

    /**
     * Set the user isInterested for a listing by adding user into the interestedUser Set.
     * @param id        ID of the saleListing
     * @param request   The HTTp request
     * @param body      The body of SaleItemInterest
     */
    @PutMapping("/listings/{id}/interest")
    public void setSaleItemInterest(
            @PathVariable Long id,
            HttpServletRequest request,
            @Valid @RequestBody SetSaleItemInterestDTO body
            ) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        logger.info("Updating the interest for the sales item (listingId={},userId={},interested={}).", id, body.getUserId(), body.getInterested());

        try {
            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, body.getUserId())) {
                throw new InsufficientPermissionResponseException("User cannot change listing interest of another user");
            }

            var user = userRepository.findById(body.getUserId())
                    .orElseThrow(() -> new ValidationResponseException("User does not exist"));

            var saleItem = saleItemRepository.findById(id)
                    .orElseThrow(() -> new DoesNotExistResponseException(SaleItem.class));

            var interestEvent = interestEventRepository.findInterestEventByNotifiedUserAndSaleItem(user, saleItem)
                    .orElseGet(() -> new InterestEvent(user, saleItem));
            if (Boolean.TRUE.equals(body.getInterested())) {
                saleItem.addInterestedUser(user);
            } else {
                saleItem.removeInterestedUser(user);
            }

            interestEvent.setInterested(body.getInterested());
            interestEventRepository.save(interestEvent);

            saleItemRepository.save(saleItem);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * DTO for getting the ID number of the purchasing user from a request to purchase a sale item.
     */
    @Getter
    private static class PurchaseSaleItemDTO {
        @NotNull
        private Long purchaserId;
    }

    /**
     * PUT endpoint for purchasing a sale item. A GAA/DGAA can purchase a sale item on behalf of any user, while other
     * users can only purchase a sale item for themselves. When a sale item is purchased, that sale item will be deleted
     * from the database and a record of the purchase will be added to the database.
     *
     * 200 response code - the purchase was successful.
     * 400 response code - the body of the response did not have a valid format.
     * 401 response code - there is a problem with the request's authentication token.
     * 403 response code - the user does not have permission to purchase a sale item for the user with the given ID.
     * 406 response code - the user or sale item id does not correspond to an existing user.
     *
     * @param id The ID of the sale item to be purchased.
     * @param request The HTTP request, used for validating the authentication token.
     * @param body The body of the request, used for getting the purchaser ID.
     */
    @PostMapping("listings/{id}/purchase")
    public void purchaseSaleItem(@PathVariable long id,
                                 HttpServletRequest request,
                                 @Valid @RequestBody PurchaseSaleItemDTO body) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        logger.info("Request to purchase listing (id={}) for user (id={}).", id, body.getPurchaserId());

        try {
            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, body.getPurchaserId())) {
                throw new InsufficientPermissionResponseException("You do not have permission to purchase a sale item for another user");
            }
            var purchaser = userRepository.findById(body.getPurchaserId())
                    .orElseThrow(() -> new DoesNotExistResponseException(User.class));
            var saleItem = saleItemRepository.findById(id)
                    .orElseThrow(() -> new DoesNotExistResponseException(SaleItem.class));

            var boughtSaleItem = new BoughtSaleItem(saleItem, purchaser);
            boughtSaleItemRepository.save(boughtSaleItem);

            var inventoryItem = saleItem.getInventoryItem();
            inventoryItem.sellQuantity(saleItem.getQuantity());
            inventoryItemRepository.save(inventoryItem);

            PurchasedEvent purchasedEvent = new PurchasedEvent(purchaser, boughtSaleItem);
            eventRepository.save(purchasedEvent);

            saleItemRepository.delete(saleItem);

            logger.info("Sale item (id={}) has been purchased for user (id={})", saleItem.getId(), purchaser.getUserID());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * DTO representing the response from getting the interest of a sale item
     */
    @Getter
    @ToString
    @AllArgsConstructor
    public static class GetSaleItemInterestDTO {
        private Boolean isInterested;
    }

    /**
     * Get the interestedUser Set and check does the set contain the param user.
     * @param listingId             Sale Listing id
     * @param request               The HTTP request
     * @param userId                ID of the user that perform the check for
     * @return boolean              Does the user liked the sale listing
     */
    @GetMapping("/listings/{listingId}/interest")
    public GetSaleItemInterestDTO getSaleItemsInterest(@PathVariable Long listingId,
                                           HttpServletRequest request,
                                            @RequestParam Long userId) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info("Getting interest status for sale listing (saleListingId={}).", listingId);

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, userId)) {
                throw new InsufficientPermissionResponseException("User cannot view listing interest of another user");
            }

            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new ValidationResponseException("User does not exist"));

            var saleItem = saleItemRepository.findById(listingId)
                    .orElseThrow(() -> new DoesNotExistResponseException(SaleItem.class));

            return new GetSaleItemInterestDTO(saleItem.getInterestedUsers().contains(user));
        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }
}
