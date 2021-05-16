package org.seng302.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Business;
import org.seng302.entities.InventoryItem;
import org.seng302.entities.Product;
import org.seng302.entities.SaleItem;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.InventoryItemRepository;
import org.seng302.persistence.SaleItemRepository;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

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

    @PostMapping("/businesses/{id}/listings")
    public void addSaleItemToBusiness(@PathVariable Long id, @RequestBody JSONObject saleItemInfo, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            logger.info(String.format("Adding sales item to business (businessId=%d).", id));
            Business business = businessRepository.getBusinessById(id);
            business.checkSessionPermissions(request);

            if (saleItemInfo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sale item creation info not provided");
            }
            Object inventoryItemIdObj = saleItemInfo.get("inventoryItemId");
            if (!(inventoryItemIdObj instanceof Number)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inventoryItemId not a number");
            }
            long inventoryItemId = ((Number)inventoryItemIdObj).longValue();

            Optional<InventoryItem> inventoryItem = inventoryItemRepository.findById(inventoryItemId);
            if (
                    inventoryItem.isEmpty() ||
                    !inventoryItem.get().getBusiness().getId().equals(business.getId())
            ) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inventory item does not exist for this business");
            }

            if (!(saleItemInfo.get("quantity") instanceof Integer)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity not a integer");
            }

            SaleItem saleItem = new SaleItem.Builder()
                    .withInventoryItem(inventoryItem.get())
                    .withQuantity((Integer)saleItemInfo.get("quantity"))
                    .withPrice(saleItemInfo.getAsString("price"))
                    .withMoreInfo(saleItemInfo.getAsString("moreInfo"))
                    .withCloses(saleItemInfo.getAsString("closes"))
                    .build();
            saleItemRepository.save(saleItem);


            response.setStatus(201);
        } catch (Exception error) {
            logger.error(error.getMessage());
            throw error;
        }
    }
}
