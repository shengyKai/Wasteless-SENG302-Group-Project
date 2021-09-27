package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.BoughtSaleItem;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface BoughtSaleItemRepository extends CrudRepository<BoughtSaleItem, Long>, JpaSpecificationExecutor<BoughtSaleItem> {
}
