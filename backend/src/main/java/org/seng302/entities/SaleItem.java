package org.seng302.entities;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoUnit.DAYS;

@NoArgsConstructor
@Entity
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleId;

    @ManyToOne
    @JoinColumn(name = "inventory_item_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private InventoryItem inventoryItem;  // Item up for sale

    @Column(name = "quantity", nullable = false)
    private int quantity = 0;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "more_info")
    private String moreInfo;

    @Column(name = "created")
    private Instant created;

    @Column(name = "closes")
    private LocalDate closes;  // Defaults to expiry date of product being sold


    // Getters and Setters

    /**
     * Get the sale id
     * @return primary key for sale items
     */
    public Long getSaleId() { return saleId; }

    /**
     * Autogenerated id
     * @param saleId unique identifier
     */
    public void setSaleId(Long saleId) { this.saleId = saleId; }

    /**
     * Get the inventory item that this is selling
     * @return inventory item
     */
    public InventoryItem getInventoryItem() { return inventoryItem; }

    /**
     * Associate an item in the inventory to the sale item
     * Compulsary: just about everything else relies on this
     * @param item from inventory
     */
    public void setInventoryItem(InventoryItem item) {
        if (item != null) {
            this.inventoryItem = item;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot sell something that is not in your inventory");
        }
    }

    /**
     * Get the quantity up for sale
     * @return quantity
     */
    public int getQuantity() { return quantity; }

    /**
     * Cannot be more than there is in the inventory
     * @param quantity that is for sale
     */
    public void setQuantity(int quantity) {
        int diff = this.quantity - quantity;
        if (quantity > 0 && diff <= inventoryItem.getRemainingQuantity()) {
            this.quantity = quantity;
            inventoryItem.setRemainingQuantity(inventoryItem.getRemainingQuantity() + diff);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a number of items between 1 and your current stock not on sale");
        }
    }

    /**
     * Get the price
     * @return price
     */
    public BigDecimal getPrice() { return price; }

    /**
     * Defaults to single price * quantity
     * @param price of sale item
     */
    public void setPrice(String price) {
        try {
            BigDecimal newPrice = new BigDecimal(price);
            if (newPrice.compareTo(BigDecimal.ZERO) >= 0) {
                this.price = newPrice;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a positive number");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid number");
        }
    }

    /**
     * Automatically suggest price to be price per item * quantity
     */
    public BigDecimal autoPrice() {
        if (inventoryItem.getPricePerItem() == null) {
            return null;
        } else {
            return inventoryItem.getPricePerItem().multiply(new BigDecimal(this.quantity));
        }
    }

    /**
     * Get more info on the sale item
     * @return more info
     */
    public String getMoreInfo() { return moreInfo; }

    /**
     * Set more info to associate with sale item
     * @param moreInfo more info on item
     */
    public void setMoreInfo(String moreInfo) {
        if (moreInfo == null || moreInfo.length() == 0) {
            this.moreInfo = null;
            return;
        }
        if (moreInfo.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extra sale information must not be longer than 200 characters");
        }
        if (!moreInfo.matches("^[\\p{Space}\\d\\p{Punct}\\p{L}]*$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extra sale info must only contain letters, numbers, whitespace and punctuation");
        }
        this.moreInfo = moreInfo;
    }
    /**
     * Get the creation date
     * @return creation date
     */
    public Instant getCreated() { return created; }
    /**
     * Set the creation date to today (set in builder)
     * @param created date
     */
    public void setCreated(Instant created) { this.created = created; }
    /**
     * Get the close date
     * @return close date
     */
    public LocalDate getCloses() { return closes; }

    /**
     * Defaults to expiry date of product
     * @param closes date
     */
    public void setCloses(String closes) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate closeDate = LocalDate.parse(closes, dateTimeFormatter);
        if (inventoryItem.getExpires().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This product is already expired");
        } else if (closeDate.isAfter(LocalDate.now().minus(1, DAYS))) {
            this.closes = closeDate;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot set close dates in the past");
        }
    }

    public void setCloses() {
        if (inventoryItem.getExpires().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This product is already expired");
        }
        this.closes = inventoryItem.getExpires();
    }

    /**
     * Gets the business owning this sales item
     * @return business owning this sales item
     */
    public Business getBusiness() {
        return this.inventoryItem.getBusiness();
    }

    /**
     * Builder for Sale Item
     */
    public static class Builder {
        private InventoryItem inventoryItem;  // Item up for sale
        private int quantity;
        private String price;
        private String moreInfo;
        private String closes;

        /**
         * Sets the inventory item to be sold
         * @param inventoryItem to be sold
         * @return Builder with inventory item set
         */
        public Builder withInventoryItem(InventoryItem inventoryItem) {
            this.inventoryItem = inventoryItem;
            return this;
        }

        /**
         * Sets the quantity to be sold, less than total quantity in inventory - currently being sold
         * @param quantity to be sold
         * @return Builder with quantity set
         */
        public Builder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * Sets the price to sell the products at
         * @param price of sale
         * @return Builder with price set
         */
        public Builder withPrice(String price) {
            this.price = price;
            return this;
        }

        /**
         * Sets any extra information the seller wants to attach to the sale item
         * @param moreInfo description
         * @return Builder with more info set
         */
        public Builder withMoreInfo(String moreInfo) {
            this.moreInfo = moreInfo;
            return this;
        }

        /**
         * Sets the close date of the sale, defaults to expiry date of product if not called
         * @param closes date
         * @return Builder with close date set
         */
        public Builder withCloses(String closes) {
            this.closes = closes;
            return this;
        }

        /**
         * Builds the Sale Item
         * @return Sale Item
         */
        public SaleItem build() {
            SaleItem saleItem = new SaleItem();
            saleItem.setInventoryItem(this.inventoryItem);
            saleItem.setMoreInfo(this.moreInfo);
            saleItem.setCreated(Instant.now());
            if (closes != null) {
                saleItem.setCloses(this.closes);
            } else {
                saleItem.setCloses();
            }
            saleItem.setPrice(this.price);
            saleItem.setQuantity(this.quantity);  // Set last because it alters other objects so a build fail in something else causes problems
            return saleItem;
        }
    }
}
