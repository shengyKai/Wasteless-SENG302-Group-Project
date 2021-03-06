package org.seng302.leftovers.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity that represents a sale item that has been sold
 */
@Entity
public class BoughtSaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "like_count", nullable = false)
    private int interestCount;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "sale_date", nullable = false)
    private Instant saleDate;

    @Column(name = "listing_date", nullable = false)
    private Instant listingDate;

    /**
     * Constructor for a BoughtSaleItem from a sale item that is being sold
     * and a user that has bought the sale item
     * @param saleItem Sale item to copy values from
     * @param buyer Buyer of the sale item
     */
    public BoughtSaleItem(SaleItem saleItem, User buyer) {
        this.buyer = buyer;
        this.product = saleItem.getProduct();
        this.interestCount = saleItem.getLikeCount();
        this.price = saleItem.getPrice();
        this.quantity = saleItem.getQuantity();
        this.saleDate = Instant.now();
        this.listingDate = saleItem.getCreated();
    }

    /**
     * Empty constructor required by JPA
     */
    protected BoughtSaleItem() {}

    /**
     * Gets the id (will be unique among BoughtSaleItems)
     * @return BoughtSaleItem id
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the user that bought the sale item
     * @return Sale item buyer
     */
    public User getBuyer() {
        return buyer;
    }

    /**
     * Gets the product that was sold
     * @return Product that was sold
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Gets the number of users that liked the sale item before it was sold
     * @return Number of likes the sale item had
     */
    public int getInterestCount() {
        return interestCount;
    }

    /**
     * Gets the price the sale item was sold at
     * @return Sale price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Gets the number of products sold
     * @return Number of sold products
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the moment in time that the sale occurred at
     * @return Sale date and time
     */
    public Instant getSaleDate() {
        return saleDate;
    }

    /**
     * Gets the moment that the sale item was created
     * @return Date and time the sale item was listed
     */
    public Instant getListingDate() {
        return listingDate;
    }

    @Override
    public String toString() {
        return "BoughtSaleItem{" +
                "id=" + id +
                ", likeCount=" + interestCount +
                ", price=" + price +
                ", quantity=" + quantity +
                ", saleDate=" + saleDate +
                ", listingDate=" + listingDate +
                '}';
    }
}
