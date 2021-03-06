package org.seng302.leftovers.entities;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.seng302.leftovers.exceptions.ValidationResponseException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Entity that represents a listing which is a subset of items within an
 * inventory entry that will be sold at a given price
 */
@NoArgsConstructor
@Entity
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name="likes",
            joinColumns = {@JoinColumn(name="sale_item_id")},
            inverseJoinColumns = {@JoinColumn(name="user_id")}
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<User> interestedUsers = new HashSet<>();

    @Formula("(SELECT count(*) FROM likes l WHERE l.sale_item_id = id)")
    private int likeCount;

    // Getters and Setters

    /**
     * Gets the number of users that have bookmarked this item
     * @return Number of users that have bookmarked this item
     */
    public int getLikeCount() {
        return likeCount;
    }

    /**
     * Add a user to the list of users that like this sale item
     * @param user User to add
     */
    public void addInterestedUser(User user) {
        interestedUsers.add(user);
    }

    /**
     * Remove a user from the list of users that like this sale item
     * @param user User to remove
     */
    public void removeInterestedUser(User user) {
        interestedUsers.remove(user);
    }

    /**
     * Get the list of users that have liked this sale item
     * @return List of users that like this sale item
     */
    public Set<User> getInterestedUsers() {
        return interestedUsers;
    }

    /**
     * Get the sale id
     * @return primary key for sale items
     */
    public Long getId() { return id; }

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
            throw new ValidationResponseException("Cannot sell something that is not in your inventory");
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
        if (quantity <= 0) {
            throw new ValidationResponseException("Quantity must be greater than 0");
        }

        int diff = this.quantity - quantity;
        inventoryItem.setRemainingQuantity(inventoryItem.getRemainingQuantity() + diff);
        this.quantity = quantity;
    }

    /**
     * Get the price
     * @return price
     */
    public BigDecimal getPrice() { return price; }

    /**
     * Defaults to single price * quantity
     * @param newPrice of sale item
     */
    public void setPrice(BigDecimal newPrice) {
        try {
            if (newPrice.compareTo(BigDecimal.ZERO) >= 0) {
                this.price = newPrice;
            } else {
                throw new ValidationResponseException("Please enter a positive number");
            }
        } catch (Exception e) {
            throw new ValidationResponseException("Please enter a valid number");
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
            throw new ValidationResponseException("Extra sale information must not be longer than 200 characters");
        }
        if (!moreInfo.matches("^[\\p{Space}\\d\\p{Punct}\\p{L}]*$")) {
            throw new ValidationResponseException("Extra sale info must only contain letters, numbers, whitespace and punctuation");
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
     * @param newCloses date
     */
    public void setCloses(LocalDate newCloses) {
        if (inventoryItem.getExpires().isBefore(LocalDate.now())) {
            throw new ValidationResponseException("This product is already expired");
        } else if (newCloses.isAfter(LocalDate.now().minus(1, DAYS))) {
            this.closes = newCloses;
        } else {
            throw new ValidationResponseException("You cannot set close dates in the past");
        }
    }

    public void setCloses() {
        if (inventoryItem.getExpires().isBefore(LocalDate.now())) {
            throw new ValidationResponseException("This product is already expired");
        }
        this.closes = inventoryItem.getExpires();
    }

    /**
     * Gets the product owning this sales item
     * @return product owning this sales item
     */
    public Product getProduct() {
        return this.inventoryItem.getProduct();
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
        private BigDecimal price;
        private String moreInfo;
        private LocalDate closes;

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
            if (price == null) {
                this.price = null;
                return this;
            }
            try {
                this.price = new BigDecimal(price);
            } catch (NumberFormatException ignored) {
                throw new ValidationResponseException("The price is not a number");
            }
            return this;
        }

        /**
         * Sets the price to sell the products at
         * @param price of sale
         * @return Builder with price set
         */
        public Builder withPrice(BigDecimal price) {
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
         * @param closesString date
         * @return Builder with close date set
         */
        public Builder withCloses(String closesString) {
            if (closesString != null) {
                this.closes = LocalDate.parse(closesString);
            } else {
                this.closes = null;
            }
            return this;
        }

        /**
         * Sets the close date of the sale, defaults to expiry date of product if not called
         * @param closes date
         * @return Builder with close date set
         */
        public Builder withCloses(LocalDate closes) {
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
