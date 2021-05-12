package org.seng302.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Business;
import org.seng302.entities.InventoryItem;
import org.seng302.entities.Product;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.ProductRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@RestController
public class InventoryController {

    private final BusinessRepository businessRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;
    private static final Logger logger = LogManager.getLogger(InventoryController.class.getName());

    @Autowired
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
        logger.info(String.format("Attempting to add and inventory item for business=%d", businessId));
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

        InventoryItem item = new InventoryItem.Builder()
                .withProduct(product)
                .withPricePerItem(inventory.getAsNumber("pricePerItem").doubleValue())
                .withQuantity(inventory.getAsNumber("quantity").intValue())
                .withBestBefore(inventory.getAsString("bestBefore"))
                .withSellBy(inventory.getAsString("sellBy"))
                .withManufactured(inventory.getAsString("manufactured"))
                .withExpires(inventory.getAsString("expires"))
                .withTotalPrice(inventory.getAsNumber("totalPrice").doubleValue())
                .build();

        inventoryItemRepository.save(item);
    }
}
