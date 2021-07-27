package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.InventoryItem;
import org.seng302.leftovers.entities.Product;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.InventoryItemRepository;
import org.seng302.leftovers.persistence.ProductRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.JsonTools;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.seng302.leftovers.tools.SearchHelper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Repeatable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.Comparator;

@RestController
public class InventoryController {

    private final BusinessRepository businessRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;
    private static final Logger logger = LogManager.getLogger(InventoryController.class.getName());

    // @Autowired
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
            @RequestBody JSONObject inventory) throws Exception {
        String message = String.format("Attempting to add and inventory item for business=%d", businessId);
        logger.info(message);
        try {
            // get business + sanity
            Business business = businessRepository.getBusinessById(businessId);
            // check business perms
            business.checkSessionPermissions(request);
            // check body exists
            if (inventory == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory information not provided");
            }
            // get productCode from body
            String productCode = inventory.getAsString("productId");
            // sanity on product
            Product product = productRepository.getProduct(business, productCode);
            Integer quantity = getQuantityFromInventoryJson(inventory);

            InventoryItem item = new InventoryItem.Builder().withProduct(product)
                    .withPricePerItem(inventory.getAsString("pricePerItem")).withQuantity(quantity)
                    .withBestBefore(inventory.getAsString("bestBefore")).withSellBy(inventory.getAsString("sellBy"))
                    .withManufactured(inventory.getAsString("manufactured"))
                    .withExpires(inventory.getAsString("expires")).withTotalPrice(inventory.getAsString("totalPrice"))
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
                               @RequestBody JSONObject invItemInfo) throws Exception {
        logger.info("Attempting to modify the inventory {} for the business {}", invItemId, businessId);

        Business business = businessRepository.getBusinessById(businessId);
        if (business == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The business with the id %d does not exist", businessId));
        }
        business.checkSessionPermissions(request);

        InventoryItem invItem = inventoryItemRepository.getInventoryItemByBusinessAndId(business, invItemId);
        if (invItem == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The inventory item with the id %d does not exist", invItemId));
        }

        if (invItemInfo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No JSON request body was provided");
        }

        //assuming all exceptions are related to bad requests since only data is being save below
        try {
            String newProductCode = invItemInfo.getAsString("productId");
            Product product = productRepository.findByBusinessAndProductCode(business, newProductCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The product with the given id does not exist within the business's catalogue"));
            invItem.setProduct(product);

            invItem.setQuantity((int) invItemInfo.getAsNumber("quantity"));

            String pricePerItem = invItemInfo.getAsString("pricePerItem");
            if (pricePerItem != null) {
                invItem.setPricePerItem(BigDecimal.valueOf(Double.parseDouble(pricePerItem)));
            } else {
                invItem.setPricePerItem(null);
            }
            String totalPrice = invItemInfo.getAsString("totalPrice");
            if (totalPrice != null) {
                invItem.setTotalPrice(BigDecimal.valueOf(Double.parseDouble(totalPrice)));
            } else {
                invItem.setTotalPrice(null);
            }

            String manufactured = invItemInfo.getAsString("manufactured");
            String sellBy = invItemInfo.getAsString("sellBy");
            String bestBefore = invItemInfo.getAsString("bestBefore");
            String expires = invItemInfo.getAsString("expires");
            invItem.setDates(manufactured, sellBy, bestBefore, expires);

            inventoryItemRepository.save(invItem);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.warn(exception);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The data provided was invalid");
        }
    }

    /**
     * Parse the inventory JSON to get quantity as an integer, or throw a response
     * status exception if quantity is not an integer.
     * 
     * @param inventory A JSON object representing an inventory item.
     * @return The number from the quantity field of the JSON.
     */
    private Integer getQuantityFromInventoryJson(JSONObject inventory) {
        int quantity;
        try {
            quantity = Integer.parseInt(inventory.getAsString("quantity"));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided quantity is not a valid number");
        }
        return quantity;
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
    public JSONObject getInventory(@PathVariable(name = "id") Long businessId,
                                HttpServletRequest request,
                                @RequestParam(required = false) String orderBy,
                                @RequestParam(required = false) Integer page,
                                @RequestParam(required = false) Integer resultsPerPage,
                                @RequestParam(required = false) Boolean reverse) {
        try {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        logger.info(() -> String.format("Getting inventory item for business (businessId=%d).", businessId));
        Business business = businessRepository.getBusinessById(businessId);

        Sort.Direction direction = SearchHelper.getSortDirection(reverse);
        Sort.Order sortOrder = getInventoryItemOrder(orderBy, direction);

        PageRequest pageRequest = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));

        Page<InventoryItem> result = inventoryItemRepository.findAllForBusiness(business, pageRequest);
        
        return JsonTools.constructPageJSON(result.map(InventoryItem::constructJSONObject));

    } catch (Exception error) {
        logger.error(error.getMessage());
        throw error;
    }

    }

    private static final Set<String> VALID_ORDERINGS = Set.of("productCode", "name", "description", "manufacturer", "recommendedRetailPrice", "created", "quantity", "pricePerItem", "totalPrice", "manufactured", "sellBy", "bestBefore", "expires");

    private Sort.Order getInventoryItemOrder(String orderBy, Sort.Direction direction) {
        if (orderBy == null) orderBy = "productCode";
        else if (!VALID_ORDERINGS.contains(orderBy)) {
            logger.error("Invalid inventory item ordering given: {}", orderBy);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided ordering is invalid");
        }

        if (orderBy.equals("name")) {
            orderBy = "product.name";
        }
        else if (orderBy.equals("description")) {
            orderBy = "product.description";
        }
        else if (orderBy.equals("manufacturer")) {
            orderBy = "product.manufacturer";
        }
        else if (orderBy.equals("recommendedRetailPrice")) {
            orderBy = "product.recommendedRetailPrice";
        }
        else if (orderBy.equals("created")) {
            orderBy = "product.created";
        }
        return new Sort.Order(direction, orderBy).ignoreCase();
    }

    /**
     * Sort inventory by a key. Can reverse results. If the inventory item attribute is null, it will be 
     * sorted to the last. If reversed, the null related inventory item will be the first.
     * 
     * @param key     Key to order products by.
     * @param reverse Reverse results.
     * @return Inventory Comparator
     */
    Comparator<InventoryItem> sortInventory(String key, Boolean reverse) {
        key = key == null ? "productCode" : key;

        Comparator<InventoryItem> sort;
        switch (key) {
            case "name":
                sort = Comparator.comparing(inventoryItem -> inventoryItem.getProduct().getName(), String.CASE_INSENSITIVE_ORDER);
                break;

            case "description":
                sort = Comparator.comparing(inventoryItem -> inventoryItem.getProduct().getDescription(), 
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;

            case "manufacturer":
                sort = Comparator.comparing(inventoryItem -> inventoryItem.getProduct().getManufacturer(),
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;

            case "recommendedRetailPrice":
                sort = Comparator.comparing(inventoryItem -> inventoryItem.getProduct().getRecommendedRetailPrice(), 
                Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "created":
                sort = Comparator.comparing(inventoryItem -> inventoryItem.getProduct().getCreated());
                break;

            case "quantity":
                sort = Comparator.comparing(InventoryItem::getQuantity);
                break;

            case "pricePerItem":
                sort = Comparator.comparing(InventoryItem::getPricePerItem, 
                Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "totalPrice":
                sort = Comparator.comparing(InventoryItem::getTotalPrice, 
                Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "manufactured":
                sort = Comparator.comparing(InventoryItem::getManufactured, 
                Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "sellBy":
                sort = Comparator.comparing(InventoryItem::getSellBy, 
                Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "bestBefore":
                sort = Comparator.comparing(InventoryItem::getBestBefore, 
                Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "expires":
                sort = Comparator.comparing(InventoryItem::getExpires);
                break;

            default:
                sort = Comparator.comparing(inventoryItem -> inventoryItem.getProduct().getProductCode());
                break;
        }

        if (Boolean.TRUE.equals(reverse)) {
            sort = sort.reversed();
        }

        return sort;
    }
}
