package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.InventoryItem;
import org.seng302.leftovers.entities.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;





@Repository
public interface InventoryItemRepository extends CrudRepository<InventoryItem, Long> {

    /**
     * Finds all the inventory items for a given business
     * @param business Business to get all inventory items form
     * @return List of inventory items for the business
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.product.business = :business")
    Page<InventoryItem> findAllForBusiness(@Param("business") Business business, Pageable pageable);

    /**
     * Gets a inventory item from the repository
     * If there is no inventory for the provided business and inventoryItemId
     * then a 406 not acceptable is thrown.
     *
     * @param business Business this inventory item must belong to
     * @param inventoryItemId Inventory item id to search for
     * @return Inventory id for business and inventoryItemId
     */
    default InventoryItem getInventoryItemByBusinessAndId(Business business, Long inventoryItemId) {
        Optional<InventoryItem> inventoryItem = findById(inventoryItemId);
        if (
                inventoryItem.isEmpty() ||
                !inventoryItem.get().getBusiness().getId().equals(business.getId())
        ) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "inventory item does not exist for this business");
        }
        return inventoryItem.get();
    }
}
