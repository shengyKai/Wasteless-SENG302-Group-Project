package org.seng302.entities;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.persistence.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

@NoArgsConstructor
@Entity
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price_per_item")
    private BigDecimal pricePerItem;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

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

// Getters 
    /**
     * Returns id in db table
     * @return id in db table
     */
    public Long getId() {
        return id;
    }
    /**
     * Returns the product
     * @return product
     */
    public Product getProduct() {
        return product;
    }
    /**
     * Return the quantity of items/products
     * @return quantity of items/products
     */
    public int getQuantity() {
        return quantity;
    }
    /**
     * Returns price of per item
     * @return price of per item
     */
    public BigDecimal getPricePerItem() {
        return pricePerItem;
    }
    /**
     * Total price based on price per item and quantity
     * @return total price
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    /**
     * Returns date of when the product was manufactured
     */
    public Date getManufactured() {
        return manufactured;
    }
    /**
     * Returns date of when the product need to get sell by
     */
    public Date getSellBy() {
        return sellBy;
    }
    /**
     * Returns date of Best Before for the product
     */
    public Date getBestBefore() {
        return bestBefore;
    }
    /**
     * Returns date of expires of the product
     */
    public Date getExpires() {
        return expires;
    }
    /**
     * Returns creation date of the item in Inventory
     */
    public Date getCreationDate() {
        return creationDate;
    }   
    
//Setters
    /**
     * Sets the id in db table
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the product
     * @param product
     */
    public void setProduct(Product product) throws ResponseStatusException {
        if (product != null) {
            this.product = product;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No product was provided");
        }
    }
    /**
     * Sets the quantity of items/products
     * @param quantity
     */
    public void setQuantity(Integer quantity) throws ResponseStatusException {
        if (quantity > 0) {
            this.quantity = quantity;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantity less than 1 was provided");
        }
    }
    /**
     * Sets the price of per item
     * @param pricePerItem price of per item
     */
    public void setPricePerItem(BigDecimal pricePerItem) {
        if (pricePerItem != null) {
            if (pricePerItem.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price per item must not be less than 0");
            }
            if (pricePerItem.compareTo(new BigDecimal(10000)) >= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price per item must be less that 100,00");
            }
        }
        this.pricePerItem = pricePerItem;
    }
    /**
     * Sets and calculates the total price based on the price per item and quantity
     */
    public void setTotalPrice() {
        if (this.pricePerItem != null) {
            this.totalPrice = this.pricePerItem.multiply(new BigDecimal(this.quantity));
        }
    }
    /**
     * Sets the total price for the products
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        if (totalPrice != null) {
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total price must not be less than 0 ");
        }
        if (totalPrice.compareTo(new BigDecimal(1000000)) >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total price must be less than 1,000,000");
        }
    }
    this.totalPrice = totalPrice;
    }
    /**
     * Sets the date of when the product was manufactured
     * @param manufactured the date when the product was manufactured
     */
    public void setManufactured(Date manufactured) {
        if (manufactured == null) {
            this.manufactured = null;
            return;
        }
        LocalDate dateOfManufactured = manufactured.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.minusDays(1);               //at least 1 day earlier
        if (dateOfManufactured.compareTo(acceptDate) > 0) {     //is in the future
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The manufactured date cannot be in the future");
        }
        this.manufactured = manufactured;     
    }
    /**
     * Sets the date of when the product need to get sell by
     * @param sellBy the date when the product need to get sell by
     */
    public void setSellBy(Date sellBy) {
        if (sellBy == null) {
            this.sellBy = null;
            return;
        }
        LocalDate dateOfSellBy = sellBy.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                //at least 1 day later
        if (dateOfSellBy.compareTo(acceptDate) < 0) {           //is in the past
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Sell By date cannot be in the past");
    }
        this.sellBy = sellBy;
    }
    /**
     * Sets the date of Best Before for the product
     * @param bestBefore the date of Best Before for the product
     */
    public void setBestBefore(Date bestBefore) {
        if(bestBefore == null) {
            this.bestBefore = null;
            return;
        }
        LocalDate dateOfBestBefore = bestBefore.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                    //at least 1 day later
        if (dateOfBestBefore.compareTo(acceptDate) < 0) {          //is in the past
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Best Before date cannot be in the past");
        }
        this.bestBefore = bestBefore;
    }

    /**
     * Sets the date of expires for the product
     * @param expires the date of expires for the product
     */
    public void setExpires(Date expires) throws ResponseStatusException {
        if(expires == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No expiry date was provided");
        }
        LocalDate dateOfExpires = expires.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate date = LocalDate.now();
        LocalDate acceptDate = date.plusDays(1);                    //at least 1 day later
        if (dateOfExpires.compareTo(acceptDate) < 0) {              //is in the past
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Expires date cannot be in the past");
        }
        this.expires = expires;
    }
    /**
     * Sets creation date of the item in Inventory
     */
    public void setCreationDate() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        this.creationDate = today.getTime();
    }
    /**
     * Builder for Inventory Item
     */
    public static class Builder {
        
        private Product product;
        private int quantity;
        private BigDecimal pricePerItem;
        private BigDecimal totalPrice;
        private Date manufactured;
        private Date sellBy;
        private Date bestBefore;
        private Date expires;

        /**
         * Sets the builder's productId. Required.
         * @param product The product in the inventory
         * @return Builder with the product set
         */
        public Builder withProduct(Product product) {
            this.product = product;
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
        public Builder withPricePerItem(String pricePerItem) {
            if (pricePerItem == null || pricePerItem.equals("")) {
                this.pricePerItem = null;
                return this;
            }
            try {
                this.pricePerItem = new BigDecimal(pricePerItem);
            } catch (NumberFormatException ignored) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The price per item is not a number");
            }
            return this;
        }

        /**
         * Set the builder's total price item.
         * @param totalPrice the total price for the product in the item inventory
         * @return Builder with the total price item set
         */
        public Builder withTotalPrice(String totalPrice) {
            if (totalPrice == null || totalPrice.equals("")) {
                this.totalPrice = null;
                return this;
            }
            try {
                this.totalPrice = new BigDecimal(totalPrice);
            } catch (NumberFormatException ignored) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The total price is not a number");
            }
            return this;
        }

        /**
         * Sets the builder's sell by date
         * @param manufacturedString the date when the product in the inventory was manufactured
         * @return Builder with the sell by date set
         */
        public Builder withManufactured(String manufacturedString) throws ParseException {
            if (manufacturedString != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                this.manufactured = dateFormat.parse(manufacturedString);
            } else { this.manufactured = null; }
            return this;
        }

        /**
         * Sets the builder's sell by date
         * @param sellByString the date when the product in the inventory must sell by
         * @return Builder with the sell by date set
         */
        public Builder withSellBy(String sellByString) throws ParseException {
            if (sellByString != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                this.sellBy = dateFormat.parse(sellByString);
            } else { this.sellBy = null; }
            return this;
        }

        /**
         * Sets the builder's best before date set
         * @param bestBeforeString the date the product in the inventory is best before
         * @return Builder with the best before date set
         */
        public Builder withBestBefore(String bestBeforeString) throws ParseException {
            if (bestBeforeString != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                this.bestBefore = dateFormat.parse(bestBeforeString);
            } else { this.bestBefore = null; }
            return this;
        }

        /**
         * Sets the builder's expired date set. Required
         * @param expiresString the date the product in the inventory expires. Must be disposed of after this date
         * @return Builder with the expires data set
         */
        public Builder withExpires(String expiresString) throws ParseException {
            if (expiresString == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No expiry date was provided");
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            this.expires = dateFormat.parse(expiresString);
            return this;
        }

        /**
         * Builds the inventory item
         * @return the inventory item that has just been created
         */
        public InventoryItem build() throws Exception {
            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.setProduct(this.product);
            inventoryItem.setQuantity(this.quantity);
            inventoryItem.setPricePerItem(this.pricePerItem);
            inventoryItem.setManufactured(this.manufactured);
            inventoryItem.setSellBy(this.sellBy);
            inventoryItem.setBestBefore(this.bestBefore);
            inventoryItem.setExpires(this.expires);
            inventoryItem.setCreationDate();
            if (this.totalPrice == null) {
                inventoryItem.setTotalPrice();
            } else {
                inventoryItem.setTotalPrice(this.totalPrice);
            }
            return inventoryItem;
        }
    }

    @Override
    public String toString() {
        return String.format("There are %d %s of this inventory item. They expire on %s",
                this.quantity, this.product.getName(), this.expires.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof InventoryItem)) {
            return false;
        }
        InventoryItem invItem = (InventoryItem) o;

        return
                this.id.equals(invItem.getId()) &&
                        this.product.getID().equals(invItem.getProduct().getID()) &&
                        this.quantity == invItem.getQuantity() &&
                        (this.pricePerItem == null ? invItem.getPricePerItem() == null :
                                this.pricePerItem.equals(invItem.getPricePerItem())) &&
                        (this.totalPrice == null ? invItem.getTotalPrice() == null :
                                this.totalPrice.equals(invItem.getTotalPrice())) &&
                        (this.manufactured == null ? invItem.getManufactured() == null :
                                this.manufactured.equals(invItem.getManufactured())) &&
                        (this.sellBy == null ? invItem.getSellBy() == null :
                                this.sellBy.equals(invItem.getSellBy())) &&
                        (this.bestBefore == null ? invItem.getBestBefore() == null :
                                this.bestBefore.equals(invItem.getBestBefore())) &&
                        this.expires.equals(invItem.getExpires()) &&
                        this.creationDate.equals(invItem.getCreationDate());
    }
}
