package org.seng302.leftovers.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.ResultPageDTO;
import org.seng302.leftovers.dto.saleitem.CreateSaleItemDTO;
import org.seng302.leftovers.dto.saleitem.SaleItemResponseDTO;
import org.seng302.leftovers.dto.saleitem.SetSaleItemInterestDTO;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.InventoryItem;
import org.seng302.leftovers.entities.SaleItem;
import org.seng302.leftovers.entities.event.InterestEvent;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.SaleItemRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.InterestEventRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
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
import javax.validation.Valid;
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

    public SaleController(UserRepository userRepository, BusinessRepository businessRepository, SaleItemRepository saleItemRepository, InventoryItemRepository inventoryItemRepository, InterestEventRepository interestEventRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.saleItemRepository = saleItemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.interestEventRepository = interestEventRepository;
    }

    private static final Set<String> VALID_ORDERINGS = Set.of("created", "closing", "productCode", "productName", "quantity", "price");

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
     * DTO representing the response from adding a sale item
     */
    @Getter
    @ToString
    @AllArgsConstructor
    public static class CreateSaleItemResponseDTO {
        private Long listingId;
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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory item does not exist for this business"));

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
    public ResultPageDTO<SaleItemResponseDTO> getSaleItemsForBusiness(@PathVariable Long id,
                                                                      HttpServletRequest request,
                                                                      @RequestParam(required = false) String orderBy,
                                                                      @RequestParam(required = false) Integer page,
                                                                      @RequestParam(required = false) Integer resultsPerPage,
                                                                      @RequestParam(required = false) Boolean reverse) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info("Getting sales item for business (businessId={}).", id);
            Business business = businessRepository.getBusinessById(id);

            Sort.Direction direction = SearchHelper.getSortDirection(reverse);
            List<Sort.Order> sortOrder = List.of(getSaleItemOrder(orderBy, direction));

            PageRequest pageRequest = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));

            Specification<SaleItem> specification = SearchHelper.constructSpecificationFromSaleItemsFilter(business);
            Page<SaleItem> result = saleItemRepository.findAll(specification, pageRequest);

            return new ResultPageDTO<>(result.map(SaleItemResponseDTO::new));

        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }

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
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot change listing interest of another user");
            }

            var user = userRepository.findById(body.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not exist"));

            var saleItem = saleItemRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Listing not found"));

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
}
