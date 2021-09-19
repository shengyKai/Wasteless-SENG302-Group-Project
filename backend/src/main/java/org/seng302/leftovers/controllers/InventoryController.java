package org.seng302.leftovers.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.ResultPageDTO;
import org.seng302.leftovers.dto.inventory.InventoryItemResponseDTO;
import org.seng302.leftovers.dto.inventory.UpdateInventoryItemDTO;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.InventoryItem;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Set;

@RestController
public class InventoryController {

    private final BusinessRepository businessRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;
    private static final Logger logger = LogManager.getLogger(InventoryController.class.getName());

    private static final Set<String> VALID_ORDERINGS = Set.of("productCode", "name", "description", "manufacturer", "recommendedRetailPrice", "created", "quantity", "pricePerItem", "totalPrice", "manufactured", "sellBy", "bestBefore", "expires");


    @Autowired
    public InventoryController(BusinessRepository businessRepository, InventoryItemRepository inventoryItemRepository,
            ProductRepository productRepository) {
        this.businessRepository = businessRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * POST endpoint for creating an inventory item from an existing product. Only
     * Business administrators and System administrators can perform this action
     * 
     * @param businessId The ID of the business to add the Inventory Item to
     * @param inventory  The request body containing information about to new
     *                   Inventory Item
     */
    @PostMapping("/businesses/{id}/inventory")
    public void addInventory(@PathVariable(name = "id") Long businessId, HttpServletRequest request,
            @RequestBody @Valid UpdateInventoryItemDTO inventory) {
        logger.info("Attempting to add and inventory item for business={}", businessId);
        try {
            // get business + sanity
            Business business = businessRepository.getBusinessById(businessId);
            // check business perms
            business.checkSessionPermissions(request);
            // sanity on product
            Product product = productRepository.getProduct(business, inventory.getProductId());

            InventoryItem item = new InventoryItem.Builder()
                    .withProduct(product)
                    .withPricePerItem(inventory.getPricePerItem())
                    .withQuantity(inventory.getQuantity())
                    .withBestBefore(inventory.getBestBefore())
                    .withSellBy(inventory.getSellBy())
                    .withManufactured(inventory.getManufactured())
                    .withExpires(inventory.getExpires())
                    .withTotalPrice(inventory.getTotalPrice())
                    .build();

            inventoryItemRepository.save(item);
        } catch (ResponseStatusException exception) {
            logger.warn(exception);
            throw exception;
        }
    }

    /**
     * PUT endpoint for modifying an inventory item entry's attributes.
     *
     * @param businessId the id of the business that has the product associated with the inventory item in their
     *                   catalogue
     * @param invItemId the id of the inventory being modified
     */
    @PutMapping("/businesses/{businessId}/inventory/{invItemId}")
    public void modifyInvEntry(@PathVariable(name = "businessId") Long businessId,
                               @PathVariable(name = "invItemId") Long invItemId, HttpServletRequest request,
                               @RequestBody @Valid UpdateInventoryItemDTO invItemInfo) {
        logger.info("Attempting to modify the inventory {} for the business {}", invItemId, businessId);

        try {
            Business business = businessRepository.getBusinessById(businessId);
            business.checkSessionPermissions(request);

            InventoryItem invItem = inventoryItemRepository.findInventoryItemByBusinessAndId(business, invItemId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory item does not exist for this business"));

            Product product = productRepository.findByBusinessAndProductCode(business, invItemInfo.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The product with the given id does not exist within the business's catalogue"));
            invItem.setProduct(product);
            invItem.setQuantity(invItemInfo.getQuantity());
            invItem.setPricePerItem(invItemInfo.getPricePerItem());
            invItem.setTotalPrice(invItemInfo.getTotalPrice());
            invItem.setDates(invItemInfo.getManufactured(), invItemInfo.getSellBy(), invItemInfo.getBestBefore(), invItemInfo.getExpires());

            inventoryItemRepository.save(invItem);
        } catch (Exception exception) {
            logger.warn(exception);
            throw exception;
        }
    }

    /**
     * GET endpoint which will return a list of JSONs for all items in the
     * business's inventory, provide that the request comes from an authenticated
     * user who is an admin of the application or the business. If the request
     * cannot be authenticated a 401 exception is returned, if the user doesn't have
     * permission to view the inventory then a 403 exception is returned, and if the
     * business doesn't exist then a 406 exception is returned. Also includes the sorting
     * and pagination of the inventory items based on the search requirements provided
     * by the user.
     * 
     * @param businessId The id of the business to retrieve the inventory from.
     * @param request    The HTTP request, used to authenticate the user's
     *                   permissions.
     * @return Array of JSON representations of sorted and paginated items in the 
     * business's inventory.
     */
    @GetMapping("/businesses/{id}/inventory")
    public ResultPageDTO<InventoryItemResponseDTO> getInventory(@PathVariable(name = "id") Long businessId,
                                                                HttpServletRequest request,
                                                                @RequestParam(required = false) String orderBy,
                                                                @RequestParam(required = false) Integer page,
                                                                @RequestParam(required = false) Integer resultsPerPage,
                                                                @RequestParam(required = false) Boolean reverse) {
        try {
            logger.info("Getting inventory item for business (businessId={}).", businessId);
            Business business = businessRepository.getBusinessById(businessId);
            business.checkSessionPermissions(request);

            Sort.Direction direction = SearchHelper.getSortDirection(reverse);
            Sort.Order sortOrder = getInventoryItemOrder(orderBy, direction);

            PageRequest pageRequest = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));

            Specification<InventoryItem> specification = SearchHelper.constructSpecificationFromInventoryItemsFilter(business);
            Page<InventoryItem> result = inventoryItemRepository.findAll(specification, pageRequest);

            return new ResultPageDTO<>(result.map(InventoryItemResponseDTO::new));

        } catch (Exception error) {
            logger.error(error);
            throw error;
        }
    }

    private Sort.Order getInventoryItemOrder(String orderBy, Sort.Direction direction) {
        if (orderBy == null) orderBy = "productCode";
        else if (!VALID_ORDERINGS.contains(orderBy)) {
            logger.error("Invalid inventory item ordering given: {}", orderBy);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided ordering is invalid");
        }

        switch (orderBy) {
            case "productCode":
                orderBy = "product.productCode";
                break;
            case "name":
                orderBy = "product.name";
                break;
            case "description":
                orderBy = "product.description";
                break;
            case "manufacturer":
                orderBy = "product.manufacturer";
                break;
            case "recommendedRetailPrice":
                orderBy = "product.recommendedRetailPrice";
                break;
            case "created":
                orderBy = "product.created";
                break;
            default:
        }
        return new Sort.Order(direction, orderBy).ignoreCase();
    }
}
