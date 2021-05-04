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

        Optional<Product> findByProductCode(@Param("productCode") String productCode);

        /**
         * Gets a product from the repository.
         * If the product does not exist then a 406 Not Acceptable is thrown
         * If the product belongs to another business, a 403 Forbidden is thrown
         * @param business The business that has the product
         * @param productCode The productCode of the product
         * @return A product or ResponseStatusException
         */
        default Product getProductByBusinessAndProductCode(Business business, String productCode) {
                Optional<Product> product = this.findByProductCode(productCode);
                if (!product.isPresent()) {
                        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                                "The given product does not exist");
                }
                if (!product.get().getBusiness().getId().equals(business.getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "You cannot modify this product");
                }
                return product.get();
        }
}