package org.seng302.persistence;

import org.seng302.entities.Business;
import org.seng302.entities.InventoryItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryItemRepository extends CrudRepository<InventoryItem, Long> {

    /**
     * Find all then inventory items in the repository which belong to the given business.
     * @param business The business which owns the inventory items.
     * @return A list of inventory items belonging to the business.
     */
    List<InventoryItem> findAllByBusiness(@Param("business") Business business);
}
