package org.seng302.entities;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Entity
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleId;

    @ManyToOne
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;  // Item up for sale

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "more_info")
    private String moreInfo;

    @Column(name = "created")
    private Instant created;

    @Column(name = "closes")
    private LocalDate closes;  // Defaults to expiry date of product being sold


    // Getters and Setters
    public Long getSaleId() { return saleId; }

    public void setSaleId(Long saleId) { this.saleId = saleId; }

    public InventoryItem getInventoryItem() { return inventoryItem; }

    public void setInventoryItem(InventoryItem item) {
        if (item != null) {
            this.inventoryItem = item;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot sell something that is not in your inventory");
        }
    }

    public int getQuantity() { return quantity; }

    /**
     * Cannot be more than there is in the inventory
     * @param quantity that is for sale
     */
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }

    /**
     * Defaults to single price * quantity
     * @param price of sale item
     */
    public void setPrice(String price) { this.price = new BigDecimal(price); }

    public void setPrice() {
        try {
            this.price = inventoryItem.getPricePerItem().multiply(new BigDecimal(this.quantity));
        } catch (NullPointerException ignored) {} // Fails if inventory not set, has own error for that
    }

    public String getMoreInfo() { return moreInfo; }

    public void setMoreInfo(String moreInfo) { this.moreInfo = moreInfo; }

    public Instant getCreated() { return created; }

    public void setCreated(Instant created) { this.created = created; }

    public LocalDate getCloses() { return closes; }

    /**
     * Defaults to expiry date of product
     * @param closes date
     */
    public void setCloses(String closes) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        this.closes = LocalDate.parse(closes, dateTimeFormatter);
    }

    public void setCloses() {
        try {
            this.closes = inventoryItem.getExpires();
        } catch (NullPointerException ignored) {}  // Fails if inventory item not set, which has it's own error
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
         * @throws Exception from required value not being set or some other violation
         */
        public SaleItem build() throws Exception {
            SaleItem saleItem = new SaleItem();
            saleItem.setInventoryItem(this.inventoryItem);
            saleItem.setQuantity(this.quantity);
            if (price != null) {
                saleItem.setPrice(this.price);
            } else {
                saleItem.setPrice();
            }
            saleItem.setMoreInfo(this.moreInfo);
            saleItem.setCreated(Instant.now());
            if (closes != null) {
                saleItem.setCloses(this.closes);
            } else {
                saleItem.setCloses();
            }
            return saleItem;
        }
    }
}
