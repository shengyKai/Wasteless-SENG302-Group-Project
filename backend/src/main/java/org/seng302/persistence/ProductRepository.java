package org.seng302.persistence;

import org.seng302.entities.Business;
import org.seng302.entities.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long>{

        /**
         *
         * @param business the business
         * @param productCode the code of the product
         * @return a single product within the business's catalogue that matches the id of said business and the code
         * of the product
         */
        Optional<Product> findByBusinessAndProductCode(@Param("business") Business business,
                                                       @Param("productCode") String productCode);

        /**
         * Gets a product from the database that matches a given image Id. This method preforms a sanity check to ensure the
         * image does exist and if not throws a not accepted response status exception.
         * @param productRepository the product repository that connects to the database
         * @param business the business object
         * @param productCode the product code of the product
         * @return the product object that matches the business and product code
         */
        default public Product getProduct(Business business, String productCode) {
                Optional<Product> product = findByBusinessAndProductCode(business, productCode);
                if (product.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                                "the product does not exist");
                }
                return product.get();
                // The product repo is not working as expected, the product can still be retrieved even when it does not exist
                // within the business's catalogue
        }
}