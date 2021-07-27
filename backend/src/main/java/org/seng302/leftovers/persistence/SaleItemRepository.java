package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.entities.SaleItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends CrudRepository<SaleItem, Long> {

    /**
     * Finds all the sale items for a given business
     * @param business Business to get all sale items form
     * @return List of sale items for the business
     */
    @Query("SELECT s FROM SaleItem s WHERE s.inventoryItem.product.business = :business")
    Page<SaleItem> findAllForBusiness(@Param("business") Business business, Pageable pageable);
}