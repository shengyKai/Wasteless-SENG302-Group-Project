package org.seng302.Persistence;

import org.seng302.Entities.Business;
import org.seng302.Entities.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long>{

        /**
         *
         * @param business the business
         * @param productCode the code of the product
         * @return a single product within the business's catalogue that matches the id of said business and the code
         * of the product
         */
        Product findByBusinessAndProductCode(@Param("business") Business business,
                                             @Param("productCode") String productCode);
}