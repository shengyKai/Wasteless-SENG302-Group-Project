package org.seng302.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Data
@NoArgsConstructor
@ToString
@Entity
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    //@JoinColumn(name = "product_id", nullable = false)
    private String productId; //The product code

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price_per_item")
    private double pricePerItem;

    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "manufactured")
    private Date manufactured;

    @Column(name = "sell_by")
    private Date sellBy;

    @Column(name = "best_before")
    private Date bestBefore;

    @Column(name = "expires", nullable = false)
    private Date expires;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPricePerItem() {
        return pricePerItem;
    }

    public void setPricePerItem(double pricePerItem) {
        this.pricePerItem = pricePerItem;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    /**
     * Sets and calculates the total price based on the price per item and quantity
     */
    public void setTotalPrice() {
        this.totalPrice = this.quantity * this.pricePerItem;
    }

    public Date getManufactured() {
        return manufactured;
    }

    public void setManufactured(Date manufactured) {
        this.manufactured = manufactured;
    }

    public Date getSellBy() {
        return sellBy;
    }

    public void setSellBy(Date sellBy) {
        this.sellBy = sellBy;
    }

    public Date getBestBefore() {
        return bestBefore;
    }

    public void setBestBefore(Date bestBefore) {
        this.bestBefore = bestBefore;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        this.creationDate = today.getTime();
    }

    //TODO Add ToString

    /**
     * Builder for Inventory Item
     */
    public static class Builder {

        private String productId;
        private int quantity;
        private double pricePerItem;
        private Date manufactured;
        private Date sellBy;
        private Date bestBefore;
        private Date expires;

        /**
         * Sets the builder's productId. Required.
         * @param productId The productId represents the product code within the product entity
         * @return Builder with the productID set
         */
        public Builder withProductId(String productId) {
            this.productId = productId;
            return this;
        }

        /**
         * Sets the builder's quantity. Required
         * @param quantity the number of items in the inventory for this product
         * @return Builder with the quantity set
         */
        public Builder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * Sets the builder's price per item.
         * @param pricePerItem the cost for each singular item for this product in the inventory
         * @return Builder with the price per item set
         */
        public Builder pricePerItem(double pricePerItem) {
            this.pricePerItem = pricePerItem;
            return this;
        }

        /**
         * Sets the builder's sell by date
         * @param manufacturedString the date when the product in the inventory was manufactured
         * @return Builder with the sell by date set
         */
        public Builder manufactured(String manufacturedString) throws ParseException {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            this.manufactured = dateFormat.parse(manufacturedString);
            return this;
        }

        /**
         * Sets the builder's sell by date
         * @param sellByString the date when the product in the inventory must sell by
         * @return Builder with the sell by date set
         */
        public Builder sellBy(String sellByString) throws ParseException {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            this.sellBy = dateFormat.parse(sellByString);
            return this;
        }

        /**
         * Sets the builder's best before date set
         * @param bestBeforeString the date the product in the inventory is best before
         * @return Builder with the best before date set
         */
        public Builder bestBefore(String bestBeforeString) throws ParseException {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            this.bestBefore = dateFormat.parse(bestBeforeString);
            return this;
        }

        /**
         * Sets the builder's expired date set. Required
         * @param expiresString the date the product in the inventory expires. Must be disposed of after this date
         * @return Builder with the expires data set
         */
        public Builder expires(String expiresString) throws ParseException {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            this.expires = dateFormat.parse(expiresString);
            return this;
        }

        /**
         * Builds the inventory item
         * @return the inventory item that has just been created
         */
        public InventoryItem build() {
            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.setProductId(this.productId);
            inventoryItem.setQuantity(this.quantity);
            inventoryItem.setPricePerItem(this.pricePerItem);
            inventoryItem.setTotalPrice();
            inventoryItem.setManufactured(this.manufactured);
            inventoryItem.setSellBy(this.sellBy);
            inventoryItem.setBestBefore(this.bestBefore);
            inventoryItem.setExpires(this.expires);
            return inventoryItem;
        }
    }



}
