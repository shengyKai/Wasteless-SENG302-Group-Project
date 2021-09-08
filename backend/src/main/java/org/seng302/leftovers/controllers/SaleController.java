package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.InventoryItem;
import org.seng302.leftovers.entities.SaleItem;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.SaleItemRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.JsonTools;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
public class SaleController {
    private static final Logger logger = LogManager.getLogger(SaleController.class);

    private final BusinessRepository businessRepository;
    private final SaleItemRepository saleItemRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public SaleController(BusinessRepository businessRepository, SaleItemRepository saleItemRepository, InventoryItemRepository inventoryItemRepository) {
        this.businessRepository = businessRepository;
        this.saleItemRepository = saleItemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    private static final Set<String> VALID_ORDERINGS = Set.of("created", "closing", "productCode", "productName", "quantity", "price");
    private static final Set<String> VALID_SEARCH_ORDERINGS = Set.of("created", "closing", "productName", "quantity", "price", "businessName", "businessLocation");
    private static final Set<String> VALID_BUSINESS_TYPES = Set.of("Any", "Accommodation and Food Services", "Retail Trade", "Charitable organisation", "Non-profit organisation");

    /**
     * Returns the ordering term for the Sort object for sorting Sale Items
     *
     * @param orderBy   Order by term
     * @param direction Ascending or descending
     * @return Order term of Sort
     */
    private Sort.Order getSaleItemOrder(String orderBy, Sort.Direction direction) {
        if (orderBy == null) orderBy = "created";
        else if (!VALID_ORDERINGS.contains(orderBy)) {
            logger.error("Invalid sale item ordering given: {}", orderBy);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided ordering is invalid");
        }
        if (orderBy.equals("productCode")) {
            orderBy = "inventoryItem.product.productCode";
        } else if (orderBy.equals("productName")) {
            orderBy = "inventoryItem.product.name";
        } else if (orderBy.equals("closing")) {
            orderBy = "closes";
        }
        return new Sort.Order(direction, orderBy).ignoreCase();
    }

    /**
     * Adds a sale item to a given business
     *
     * @param id           The id of the business to add to
     * @param saleItemInfo The content of the sale item
     * @return The ID of the new listing
     */
    @PostMapping("/businesses/{id}/listings")
    public JSONObject addSaleItemToBusiness(@PathVariable Long id, @RequestBody JSONObject saleItemInfo, HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Adding sales item to business (businessId=%d).", id));
            Business business = businessRepository.getBusinessById(id);
            business.checkSessionPermissions(request);

            if (saleItemInfo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sale item creation info not provided");
            }
            Object inventoryItemIdObj = saleItemInfo.get("inventoryItemId");
            if (!(inventoryItemIdObj instanceof Number)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inventoryItemId not a number");
            }
            InventoryItem inventoryItem;
            try {
                inventoryItem = inventoryItemRepository.getInventoryItemByBusinessAndId(
                        business,
                        ((Number) inventoryItemIdObj).longValue()
                );
            } catch (ResponseStatusException exception) {
                // Make sure to return a 400 instead of a 406
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getReason());
            }

            if (!(saleItemInfo.get("quantity") instanceof Integer)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity not a integer");
            }

            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(inventoryItem)
                    .withQuantity((Integer) saleItemInfo.get("quantity"))
                    .withPrice(saleItemInfo.getAsString("price"))
                    .withMoreInfo(saleItemInfo.getAsString("moreInfo"))
                    .withCloses(saleItemInfo.getAsString("closes"))
                    .build();
            saleItem = saleItemRepository.save(saleItem);

            response.setStatus(201);
            var object = new JSONObject();
            object.put("listingId", saleItem.getSaleId());
            return object;
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
    public JSONObject getSaleItemsForBusiness(@PathVariable Long id,
                                              HttpServletRequest request,
                                              @RequestParam(required = false) String orderBy,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer resultsPerPage,
                                              @RequestParam(required = false) Boolean reverse) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(() -> String.format("Getting sales item for business (businessId=%d).", id));
            Business business = businessRepository.getBusinessById(id);

            Sort.Direction direction = SearchHelper.getSortDirection(reverse);
            List<Sort.Order> sortOrder = List.of(getSaleItemOrder(orderBy, direction));

            PageRequest pageRequest = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));

            Specification<SaleItem> specification = SearchHelper.constructSpecificationFromSaleItemsFilter(business);
            Page<SaleItem> result = saleItemRepository.findAll(specification, pageRequest);

            return JsonTools.constructPageJSON(result.map(SaleItem::constructJSONObject));

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
     * @param businessType type of business to restrict results to
     * @param priceLower all products must be above this price
     * @param priceUpper all products must be below this price
     * @param closeLower all products must close after this date
     * @param closeUpper all products must close before this date
     * @return JSON page of sale items
     */
    @GetMapping("/businesses/listings/search")
    public JSONObject searchSaleItems(HttpServletRequest request,
                                      @RequestParam(required = false) String basicSearchQuery,
                                      @RequestParam(required = false) String productSearchQuery,
                                      @RequestParam(required = false) String businessSearchQuery,
                                      @RequestParam(required = false) String locationSearchQuery,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer resultsPerPage,
                                      @RequestParam(required = false) Boolean reverse,
                                      @RequestParam(required = false) String orderBy,
                                      @RequestParam(required = false) String businessType,
                                      @RequestParam(required = false) String priceLower,
                                      @RequestParam(required = false) String priceUpper,
                                      @RequestParam(required = false) String closeLower,
                                      @RequestParam(required = false) String closeUpper) {
        try {
            // Check auth
            logger.info("Get sale items to match parameters.");
            AuthenticationTokenManager.checkAuthenticationToken(request);

            // Check sort ordering
            orderBy = Optional.ofNullable(orderBy).orElse("productName");
            if (!VALID_SEARCH_ORDERINGS.contains(orderBy)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OrderBy term " + orderBy + " is invalid");
            }
            List<Sort.Order> sortOrder;
            Sort.Direction direction = SearchHelper.getSortDirection(reverse);
            sortOrder = List.of(new Sort.Order(direction, orderBy ).ignoreCase());

            // Check filter options
            businessType = Optional.ofNullable(businessType).orElse("All");
            if (!VALID_BUSINESS_TYPES.contains(businessType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BusinessType term " + businessType + " is invalid");
            }
            String decimalPattern = "([0-9]*)\\.([0-9]{2})";
            BigDecimal minPrice;
            BigDecimal maxPrice;
            if (Pattern.matches(decimalPattern, priceLower) && Pattern.matches(decimalPattern, priceUpper)) {
                minPrice = new BigDecimal(priceLower);
                maxPrice = new BigDecimal(priceUpper);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price parameters were invalid");
            }

            // Create page
            PageRequest pageablePage = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));
            Specification<SaleItem> specification = SearchHelper.constructSaleItemSpecificationFromSearchQueries(basicSearchQuery, productSearchQuery, businessSearchQuery, locationSearchQuery);
            Page<SaleItem> result = saleItemRepository.findAll(specification, pageablePage);

            // TODO integrate filtering options to restrict returned sale items
            return JsonTools.constructPageJSON(result.map(SaleItem::constructJSONObject));

        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }
}
