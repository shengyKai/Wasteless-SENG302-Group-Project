package org.seng302.Entities;

import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"product_code", "business_id"})
})
@Entity
public class Product {
    // Product code must only contain uppercase letters, numbers and dashes
    // Product code have a length between 1-15
    private final String productCodeRegex = "^[-A-Z0-9]{1,15}$";


    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, name = "product_code")
    private String productCode;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private String manufacturer;

    @Column(name = "recommended_retail_price")
    private BigDecimal recommendedRetailPrice;

    @Column(nullable = false)
    private Date created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    /**
     * Get the id of the product (Is globally unique)
     * @return the id of the product
     */
    public Long getID() { return id; }

    /**
     * Get the products associated code (unique within its catalogue)
     * @return the code of the product
     */
    public String getProductCode() { return productCode; }

    /**
     * Get the name of the product
     * @return the name of the product
     */
    public String getName() { return name; }

    /**
     * Get the description of the product
     * @return the description of the product
     */
    public String getDescription() { return description; }

    /**
     * Get the manufacturer name of the product
     * @return the manufacturer name of the product
     */
    public String getManufacturer() { return manufacturer; }

    /**
     * Get the recommended retail price of the product
     * @return the recommended retailed of the product
     */
    public BigDecimal getRecommendedRetailPrice() { return recommendedRetailPrice; }

    /**
     * Get the date of when the product was created
     * @return the date of when the product was created
     */
    public Date getCreated() { return created; }

    /**
     * Get the business associated with the catalogue the product is in
     * @return the business
     */
    public Business getBusiness() { return business; }

    /**
     * Sets the code of the product
     * @param productCode the code for the product
     */
    private void setProductCode(String productCode) {
        if (productCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product code must be provided");
        }
        if (!productCode.matches(productCodeRegex)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product code must have a valid format");
        }
        this.productCode = productCode;
    }

    /**
     * Sets the name of the product
     * @param name the name of the product
     */
    public void setName(String name) {
        if (name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name must be provided");
        }
        if (name.length() == 0 || name.length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name must be between 1-50 characters long");
        }
        this.name = name;
    }

    /**
     * Sets the description of the product
     * @param description the description of the product
     */
    public void setDescription(String description) {
        if (description != null) {
            if (description.length() == 0) {
                description = null;
            } else if (description.length() > 200) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product description must not be longer than 200 characters");
            }
        }
        this.description = description;
    }

    /**
     * Sets the manufacturer name of the product
     * @param manufacturer the manufacturer name of the product
     */
    public void setManufacturer(String manufacturer) {
        if (manufacturer != null) {
            if (manufacturer.length() == 0) {
                manufacturer = null;
            } else if (manufacturer.length() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product manufacturer must not be longer than 100 characters");
            }
        }
        this.manufacturer = manufacturer;
    }

    /**
     * Sets the recommended retail price of the product
     * @param recommendedRetailPrice the RRP of the product
     */
    public void setRecommendedRetailPrice(BigDecimal recommendedRetailPrice) {
        if (recommendedRetailPrice != null) {
            if (recommendedRetailPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product recommended retail price must not be less than 0");
            }
            if (recommendedRetailPrice.compareTo(new BigDecimal(10000)) >= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product recommended retail price must be less that 100,000");
            }
        }
        this.recommendedRetailPrice = recommendedRetailPrice;
    }

    /**
     * Sets the date of when the product was created
     * @param created the date when the product was created
     */
    private void setCreated(Date created) { this.created = created; }

    /**
     * Sets the business associated with the catalogue the product is in
     * @param business the business
     */
    private void setBusiness(Business business) {
        this.business = business;
        business.addToCatalogue(this);
    }

    /**
     * Convert product to a JSON object
     */
    public JSONObject constructJSONObject() {
        JSONObject object = new JSONObject();
        object.put("id", productCode);
        object.put("name", name);
        object.put("description", description);
        object.put("manufacturer", manufacturer);
        object.put("recommendedRetailPrice", recommendedRetailPrice);
        object.put("created", created);
        return object;
    }

    /**
     * Builder for Product
     */
    public static class Builder {
        private String productCode;
        private String name;
        private String description;
        private String manufacturer;
        private BigDecimal recommendedRetailPrice;
        private Business business;

        /**
         * Sets the builder's product code. Mandatory
         * @param productCode the code of the product. Unique identifier in each business catalogue
         * @return Builder with the product code set
         */
        public Builder withProductCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        /**
         * Sets the builder's name. Mandatory
         * @param name the full name of the product
         * @return Builder with the name set
         */
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the builder's description.
         * @param description the description of the product
         * @return Builder with the description set
         */
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the builder's manufacturer name.
         * @param manufacturer the manufacturer name of the product
         * @return Builder with the description set
         */
        public Builder withManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        /**
         * Sets the builder's recommended retail price.
         * If the provided string is not a valid number then this method will throw an exception
         *
         * @param recommendedRetailPrice the recommended retail price of the product
         * @return Builder with the recommended retail price set
         */
        public Builder withRecommendedRetailPrice(String recommendedRetailPrice) {
            try {
                this.recommendedRetailPrice = new BigDecimal(recommendedRetailPrice);
            } catch (NumberFormatException ignored) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The recommended retail price is not a number");
            }
            return this;
        }

        /**
         * Sets the builder's business. Mandatory
         * @param business the business of the catalogue the product is in
         * @return Builder with the business set
         */
        public Builder withBusiness(Business business) {
            this.business = business;
            return this;
        }

        /**
         * Builds the product
         * @return the newly created product
         */
        public Product build() {
            Product product = new Product();
            product.setProductCode(this.productCode);
            product.setName(this.name);
            product.setDescription(this.description);
            product.setManufacturer(this.manufacturer);
            product.setRecommendedRetailPrice(this.recommendedRetailPrice);
            product.setBusiness(this.business);
            product.setCreated(new Date());
            return product;
        }
    }
}