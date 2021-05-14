package org.seng302.entities;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private int quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "more_info")
    private String moreInfo;

    @Column(name = "created")
    private Date created;

    @Column(name = "closes")
    private Date closes;  // Defaults to expiry date of product being sold


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
    public void setQuantity(int quantity) {
        try {
            if (quantity > 0 && quantity <= inventoryItem.getRemainingQuantity()) {
                this.quantity = quantity;
                inventoryItem.setRemainingQuantity(inventoryItem.getRemainingQuantity()-quantity);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a number of items between 1 and your current stock not on sale");
            }
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory item has been removed, you have no more to sell");
        }
    }

    /**
     * Use this for anything but initial creation or you'll domino the quantity logic relation with inventory
     * @param newQuantity new quantity to be sold, works out the difference in method
     */
    public void adjustQuantity(int newQuantity) {
        try {
            int diff = quantity - newQuantity;
            if (newQuantity > 0 && diff <= inventoryItem.getRemainingQuantity()) {
                this.quantity = newQuantity;
                inventoryItem.setRemainingQuantity(inventoryItem.getRemainingQuantity() + diff);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a number of items between 1 and your current stock not on sale");
            }
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory item has been removed, you have no more to sell");
        }
    }

    public BigDecimal getPrice() { return price; }

    /**
     * Defaults to single price * quantity
     * @param price of sale item
     */
    public void setPrice(String price) {
        try {
            this.price = new BigDecimal(price);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid number");
        }
    }

    public void setPrice() {
        try {
            if (inventoryItem.getPricePerItem() == null) {
                this.price = BigDecimal.ZERO;
            } else {
                this.price = inventoryItem.getPricePerItem().multiply(new BigDecimal(this.quantity));
            }
        } catch (NullPointerException ignored) {} // Fails if inventory not set, has own error for that
    }

    public String getMoreInfo() { return moreInfo; }

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

    public Date getCreated() { return created; }

    public void setCreated(Date created) { this.created = created; }

    public Date getCloses() { return closes; }

    /**
     * Defaults to expiry date of product
     * @param closes date
     */
    public void setCloses(String closes) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date close = dateFormat.parse(closes);
            if (close.after(new Date()) || dateFormat.format(new Date()).equals(closes)) {
                this.closes = close;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot set close dates in the past");
            }
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please set valid date");
        }
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
            saleItem.setCreated(new Date());
            if (closes != null) {
                saleItem.setCloses(this.closes);
            } else {
                saleItem.setCloses();
            }
            return saleItem;
        }
    }
}
