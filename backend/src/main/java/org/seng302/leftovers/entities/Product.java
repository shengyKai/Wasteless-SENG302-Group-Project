package org.seng302.leftovers.entities;

import org.seng302.leftovers.exceptions.ValidationResponseException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"product_code", "business_id"})
})
@Entity
public class Product implements ImageAttachment {
    // Product code must only contain uppercase letters, numbers and dashes
    // Product code have a length between 1-15
    private static final String PRODUCT_CODE_REGEX = "^[-A-Z0-9]{1,15}$";


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Instant created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @OrderColumn(name="image_order")
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name="product_id")
    private List<Image> images = new ArrayList<>();



    @Column(nullable = false)
    private String countryOfSale;

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
     * Adds a single image to the Product's list of images
     * @param image An image entity to be linked to this product.
     */
    public void addImage(Image image) {
        this.images.add(image);
    }
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
    public Instant getCreated() { return created; }

    /**
     * Get the business associated with the catalogue the product is in
     * @return the business
     */
    public Business getBusiness() { return business; }

    /**
     * Get the image object associated with this product
     * @return the image
     */
    @Override
    public List<Image> getImages() { return images; }

    /**
     * Get the name of the country which the product is being sold in.
     */
    public String getCountryOfSale() { return countryOfSale; }


    /**
     * Sets the code of the product
     * @param productCode the code for the product
     */
    public void setProductCode(String productCode) {
        if (productCode == null) {
            throw new ValidationResponseException("Product code must be provided");
        }
        if (!productCode.matches(PRODUCT_CODE_REGEX)) {
            throw new ValidationResponseException("Product code must have a valid format");
        }
        this.productCode = productCode;
    }

    /**
     * Sets the name of the product
     * @param name the name of the product
     */
    public void setName(String name) {
        if (name == null) {
            throw new ValidationResponseException("Product name must be provided");
        }
        if (name.isEmpty() || name.length() > 50) {
            throw new ValidationResponseException("Product name must be between 1-50 characters long");
        }
        if (!name.matches("^[ \\d\\p{Punct}\\p{L}]*$")) {
            throw new ValidationResponseException("Product name must only contain letters, numbers, spaces and punctuation");
        }
        this.name = name;
    }

    /**
     * Sets the description of the product
     * @param description the description of the product
     */
    public void setDescription(String description) {
        if (description == null || description.length() == 0) {
            this.description = null;
            return;
        }
        if (description.length() > 200) {
            throw new ValidationResponseException("Product description must not be longer than 200 characters");
        }
        if (!description.matches("^[\\p{Space}\\d\\p{Punct}\\p{L}]*$")) {
            throw new ValidationResponseException("Product description must only contain letters, numbers, whitespace and punctuation");
        }
        this.description = description;
    }

    /**
     * Sets the manufacturer name of the product
     * @param manufacturer the manufacturer name of the product
     */
    public void setManufacturer(String manufacturer) {
        if (manufacturer == null || manufacturer.length() == 0) {
            this.manufacturer = null;
            return;
        }
        if (manufacturer.length() > 100) {
            throw new ValidationResponseException("Product manufacturer must not be longer than 100 characters");
        }
        if (!manufacturer.matches("^[ \\d\\p{Punct}\\p{L}]*$")) {
            throw new ValidationResponseException("Product manufacturer must only contain letters, numbers, spaces and punctuation");
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
                throw new ValidationResponseException("Product recommended retail price must not be less than 0");
            }
            if (recommendedRetailPrice.compareTo(new BigDecimal(10000)) >= 0) {
                throw new ValidationResponseException("Product recommended retail price must be less that 10,000");
            }
        }
        this.recommendedRetailPrice = recommendedRetailPrice;
    }

    /**
     * Sets the images associated with the product
     * @param images the product images
     */
    public void setImages(List<Image> images) {
        this.images = images;
    }

    /**
     * Removes a given image from the list of products
     * @param image The image to remove
     */
    public void removeImage(Image image) {
        if (!this.images.contains(image)) {
            throw new ValidationResponseException("Product cannot be removed");
        }
        this.images.remove(image);
    }

    /**
     * Sets the name of the country which the product is being sold in. The country of sale must not be null, empty or
     * blank, and must contain up to 100 characters which can only be letters or spaces.
     * @param countryOfSale the name of the country where the product is to be sold.
     */
    public void setCountryOfSale(String countryOfSale) {
        if (countryOfSale == null || countryOfSale.isEmpty() || countryOfSale.isBlank()) {
            throw new ValidationResponseException("Country of sale cannot be empty");
        } else if (countryOfSale.length() > 100) {
            throw new ValidationResponseException("Country of sale must be less than 100 characters long");
        } else if (!countryOfSale.matches("[ \\p{L}]+")) {
            throw new ValidationResponseException("Country of sale contains illegal characters");
        } else {
            this.countryOfSale = countryOfSale;
        }
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
        public Builder withRecommendedRetailPrice(BigDecimal recommendedRetailPrice) {
            this.recommendedRetailPrice = recommendedRetailPrice;
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
                throw new ValidationResponseException("The recommended retail price is not a number");
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
            setBusiness(product, this.business);
            product.setCountryOfSale(this.business.getAddress().getCountry());
            setCreated(product, Instant.now());
            return product;
        }

        /**
         * Sets the date of when the product was created
         * @param created the date when the product was created
         */
        private void setCreated(Product product, Instant created) { product.created = created; }

        /**
         * Sets the business associated with the catalogue the product is in
         * @param business the business
         */
        private void setBusiness(Product product, Business business) {
            product.business = business;
            business.addToCatalogue(product);
        }
    }
}