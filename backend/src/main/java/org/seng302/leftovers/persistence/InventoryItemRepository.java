package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.InventoryItem;
import org.seng302.leftovers.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;


@Repository
public interface InventoryItemRepository extends CrudRepository<InventoryItem, Long>, JpaSpecificationExecutor<InventoryItem> {

    /**
     * Finds all the inventory items for a given business
     * @param business Business to get all inventory items form
     * @return List of inventory items for the business
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.product.business = :business")
    Page<InventoryItem> findAllForBusiness(@Param("business") Business business, Pageable pageable);

    @Query("SELECT i FROM InventoryItem i WHERE i.product.business = :business")
    List<InventoryItem> findAllForBusiness(@Param("business") Business business);

    public List<InventoryItem> findAllByProduct(@Param("product") Product product);
    /**
     * Finds an inventory item in the repository
     * If there is no inventory for the provided business and inventoryItemId
     * then a Optional.empty() is returned
     *
     * @param business Business this inventory item must belong to
     * @param inventoryItemId Inventory item id to search for
     * @return Inventory item for business and inventoryItemId if present otherwise Optional.empty()
     */
    default Optional<InventoryItem> findInventoryItemByBusinessAndId(Business business, Long inventoryItemId) {
        Optional<InventoryItem> inventoryItem = findById(inventoryItemId);
        if (
                inventoryItem.isEmpty() || !inventoryItem.get().getBusiness().getId().equals(business.getId())
        ) {
            return Optional.empty();
        }
        return inventoryItem;
    }
}
