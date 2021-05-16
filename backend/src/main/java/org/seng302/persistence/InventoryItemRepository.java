package org.seng302.persistence;

import org.seng302.entities.Business;
import org.seng302.entities.InventoryItem;
import org.seng302.entities.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.ArrayList;

@Repository
public interface InventoryItemRepository extends CrudRepository<InventoryItem, Long> {

    /**
     * Find all then inventory items in the repository which belong to the given product.
     * @param product The product which owns the inventory items.
     * @return A list of inventory items belonging to the product.
     */
    public List<InventoryItem> findAllByProduct(@Param("product") Product product);

    /**
     * This method takes a list of product items and returns all the inventory items where the product
     * of the item is in the given list. It can be used to find all items from a business's catalogue by
     * passing in the business's calalogue as the arguement.
     * @param catalogue A list of products
     * @return A list of inventory items with products that correspond to those in the catalogue.
     */
    default List<InventoryItem> getInventoryByCatalogue(List<Product> catalogue) {
        List<InventoryItem> inventory = new ArrayList<>();
        for (Product product : catalogue) {
            List<InventoryItem> productInvItems = findAllByProduct(product);
            inventory.addAll(productInvItems);
        }
        return inventory;
    }

}
