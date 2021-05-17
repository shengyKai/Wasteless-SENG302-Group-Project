package org.seng302.entities;

import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;
import org.seng302.tools.JsonTools;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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
    private LocalDate manufactured;

    @Column(name = "sell_by")
    private LocalDate sellBy;

    @Column(name = "best_before")
    private LocalDate bestBefore;

    @Column(name = "expires", nullable = false)
    private LocalDate expires;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) throws ResponseStatusException {
        if (product != null) {
            this.product = product;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No product was provided");
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) throws Exception {
        if (quantity > 0) {
            this.quantity = quantity;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantity less than 1 was provided");
        }
    }

    public BigDecimal getPricePerItem() {
        return pricePerItem;
    }

    public void setPricePerItem(String pricePerItem) {
        this.pricePerItem = new BigDecimal(pricePerItem);
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Sets and calculates the total price based on the price per item and quantity
     */
    public void setTotalPrice() {
        if (this.pricePerItem != null) {
            this.totalPrice = new BigDecimal(this.quantity).multiply(this.pricePerItem);
        }
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = new BigDecimal(totalPrice);
    }

    public LocalDate getManufactured() {
        return manufactured;
    }

    public void setManufactured(LocalDate manufactured) {
        this.manufactured = manufactured;
    }

    public LocalDate getSellBy() {
        return sellBy;
    }

    public void setSellBy(LocalDate sellBy) {
        this.sellBy = sellBy;
    }

    public LocalDate getBestBefore() {
        return bestBefore;
    }

    public void setBestBefore(LocalDate bestBefore) {
        this.bestBefore = bestBefore;
    }

    public LocalDate getExpires() {
        return expires;
    }

    public void setExpires(LocalDate expires) throws ResponseStatusException {
        if (expires != null) {
            this.expires = expires;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No expiry date was provided");
        }
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate() {
        this.creationDate = Instant.now();
    }

    /**
     * Construct a JSON representation of the inventory item. Attributes which are null will be omitted from the
     * returned JSON.
     * @return JSON representation of the inventory item.
     */
    public JSONObject constructJSONObject() {
        JSONObject json = new JSONObject();
        json.put("id", this.getId());
        json.put("product", product.constructJSONObject());
        json.put("quantity", quantity);
        json.put("pricePerItem", pricePerItem);
        json.put("totalPrice", totalPrice);
        json.put("manufactured", manufactured != null ? manufactured.toString() : null);
        json.put("sellBy", sellBy != null ? sellBy.toString() : null);
        json.put("bestBefore", bestBefore != null ? bestBefore.toString() : null);
        json.put("expires", expires.toString());
        JsonTools.removeNullsFromJson(json);
        return json;
    }

    /**
     * Builder for Inventory Item
     */
    public static class Builder {

        private Product product;
        private int quantity;
        private String pricePerItem;
        private String totalPrice;
        private LocalDate manufactured;
        private LocalDate sellBy;
        private LocalDate bestBefore;
        private LocalDate expires;

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
            this.pricePerItem = pricePerItem;
            return this;
        }

        /**
         * Set the builder's total price item.
         * @param totalPrice the total price for the product in the item inventory
         * @return Builder with the total price item set
         */
        public Builder withTotalPrice(String totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        /**
         * Sets the builder's sell by date
         * @param manufacturedString the date when the product in the inventory was manufactured
         * @return Builder with the manufactured date set
         */
        public Builder withManufactured(String manufacturedString) {
            if (manufacturedString != null) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
                this.manufactured = LocalDate.parse(manufacturedString, dateTimeFormatter);
            } else { this.manufactured = null; }
            return this;
        }

        /**
         * Sets the builder's sell by date
         * @param sellByString the date when the product in the inventory must sell by
         * @return Builder with the sell by date set
         */
        public Builder withSellBy(String sellByString) {
            if (sellByString != null) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
                this.sellBy = LocalDate.parse(sellByString, dateTimeFormatter);
            } else { this.sellBy = null; }
            return this;
        }

        /**
         * Sets the builder's best before date set
         * @param bestBeforeString the date the product in the inventory is best before
         * @return Builder with the best before date set
         */
        public Builder withBestBefore(String bestBeforeString) {
            if (bestBeforeString != null) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
                this.bestBefore = LocalDate.parse(bestBeforeString, dateTimeFormatter);
            } else { this.bestBefore = null; }
            return this;
        }

        /**
         * Sets the builder's expired date set. Required
         * @param expiresString the date the product in the inventory expires. Must be disposed of after this date
         * @return Builder with the expires data set
         */
        public Builder withExpires(String expiresString) {
            if (expiresString == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No expiry date was provided");
            }
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
            this.expires = LocalDate.parse(expiresString, dateTimeFormatter);
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
            if (pricePerItem != null) {
                inventoryItem.setPricePerItem(this.pricePerItem);
            }
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
                        ChronoUnit.SECONDS.between(this.creationDate, invItem.getCreationDate()) < 20;
    }
}
