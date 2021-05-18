package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Business;
import org.seng302.entities.InventoryItem;
import org.seng302.entities.Product;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class InventoryController {

    private final BusinessRepository businessRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;
    private static final Logger logger = LogManager.getLogger(InventoryController.class.getName());

    //@Autowired
    public InventoryController(BusinessRepository businessRepository, InventoryItemRepository inventoryItemRepository, ProductRepository productRepository) {
        this.businessRepository = businessRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.productRepository = productRepository;
    }


    /**
     * POST endpoint for creating an inventory item from an existing product.
     * Only Business administrators and System administrators can perform this action
     * @param businessId The ID of the business to add the Inventory Item to
     * @param inventory The request body containing information about to new Inventory Item
     */
    @PostMapping("/businesses/{id}/inventory")
    public void addInventory(@PathVariable(name="id") Long businessId, HttpServletRequest request, @RequestBody JSONObject inventory) throws Exception {
        String message = String.format("Attempting to add and inventory item for business=%d", businessId);
        logger.info(message);
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
        Product product = productRepository.getProductByBusinessAndProductCode(business, productCode);
        Integer quantity;
        try {
            quantity = Integer.parseInt(inventory.getAsString("quantity"));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided quantity is not a valid number");
        }


        InventoryItem item = new InventoryItem.Builder()
                .withProduct(product)
                .withPricePerItem(inventory.getAsString("pricePerItem"))
                .withQuantity(quantity)
                .withBestBefore(inventory.getAsString("bestBefore"))
                .withSellBy(inventory.getAsString("sellBy"))
                .withManufactured(inventory.getAsString("manufactured"))
                .withExpires(inventory.getAsString("expires"))
                .withTotalPrice(inventory.getAsString("totalPrice"))
                .build();

        inventoryItemRepository.save(item);
    }

    /**
     * GET endpoint which will return a list of JSONs for all items in the business's inventory, provide that the request
     * comes from an authenticated user who is an admin of the application or the business. If the request cannot be
     * authenticated a 401 exception is returned, if the user doesn't have permission to view the inventory then a 403
     * exception is returned, and if the business doesn't exist then a 406 exception is returned.
     * @param businessId The id of the business to retrieve the inventory from.
     * @param request The HTTP request, used to authenticate the user's permissions.
     * @return Array of JSON representations of items in the business's inventory.
     */
    @GetMapping("/businesses/{id}/inventory")
    public JSONArray getInventory(@PathVariable(name="id") Long businessId, HttpServletRequest request)  {
        //Todo add sorting and pagination (t127 and t171)
        String statusMessage = String.format("Get inventory of business with ID %d", businessId);
        logger.info(statusMessage);
        List<InventoryItem> inventory = getInventoryFromRequest(businessId, request);
        JSONArray jsonArray = new JSONArray();
        for (InventoryItem item : inventory) {
            jsonArray.appendElement(item.constructJSONObject());
        }
        return jsonArray;
    }

    /**
     * GET endpoint which will return the number of items in the business's inventory, provide that the request
     * comes from an authenticated user who is an admin of the application or the business. If the request cannot be
     * authenticated a 401 exception is returned, if the user doesn't have permission to view the inventory then a 403
     * exception is returned, and if the business doesn't exist then a 406 exception is returned.
     * @param businessId The id of the business to retrieve the inventory from.
     * @param request The HTTP request, used to authenticate the user's permissions.
     * @return A JSONObject with the count of the number of items in the business's inventory.
     */
    @GetMapping("/businesses/{id}/inventory/count")
    public JSONObject getInventoryCount(@PathVariable(name="id") Long businessId, HttpServletRequest request) {
        String statusMessage = String.format("Get inventory count of business with ID %d", businessId);
        logger.info(statusMessage);
        List<InventoryItem> inventory = getInventoryFromRequest(businessId, request);
        JSONObject json = new JSONObject();
        json.put("count", inventory.size());
        return json;
    }

    /**
     * This method takes the business id and the http request sent to a get endpoint, and uses them to retrieve the
     * inventory assoicated with the business. It will also add an error to the log if one is thrown due to and invalid
     * auth token, insufficient permissions or the business not existing.
     * @param businessId The ID number of the business to find the inventory of.
     * @param request The incoming HTTP request, used to check permissions.
     * @return A list of the items in the business's inventory.
     */
    private List<InventoryItem> getInventoryFromRequest(Long businessId, HttpServletRequest request) {
        try {
            Business business = businessRepository.getBusinessById(businessId);
            // Check user is logged in and has permission to act as the business
            business.checkSessionPermissions(request);
            List<Product> catalogue = productRepository.findAllByBusiness(business);
            return inventoryItemRepository.getInventoryByCatalogue(catalogue);
        } catch (ResponseStatusException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

}
