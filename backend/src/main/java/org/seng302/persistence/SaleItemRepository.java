package org.seng302.persistence;

import org.seng302.entities.Business;
import org.seng302.entities.Product;
import org.seng302.entities.SaleItem;
import org.seng302.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends CrudRepository<SaleItem, Long> {
    //@Query("SELECT s FROM SaleItem s left join fetch s.inventoryItem as i left join fetch i.product as p left join fetch p.business as b WHERE b = :cat")
    //List<SaleItem> findAllForBusiness(@Param("cat") Business business);


    @Query("SELECT s FROM SaleItem s WHERE s.inventoryItem.product.business = :business")
    List<SaleItem> findAllForBusiness(@Param("business") Business business);
}