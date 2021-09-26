package org.seng302.leftovers.entities;

import org.seng302.leftovers.dto.business.BusinessType;
import org.seng302.leftovers.dto.business.Rank;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.seng302.leftovers.exceptions.InsufficientPermissionResponseException;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.seng302.leftovers.tools.AuthenticationTokenManager;

import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Business implements ImageAttachment {

    //Minimum age to create a business
    private static final int MINIMUM_AGE = 16;
    private static int POINTS_PER_SALE_LISTING = 1;
    private static final String TEXT_REGEX = "[ \\p{L}0-9\\p{Punct}]*";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Location address;
    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private BusinessType businessType;
    @Column
    private Instant created;
    @Column(nullable = false)
    private int points;
    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Rank rank = Rank.BRONZE;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "business", cascade = CascadeType.REMOVE)
    private List<Product> catalogue = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User primaryOwner;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name="business_admins",
            joinColumns = {@JoinColumn(name="business_id")},
            inverseJoinColumns = {@JoinColumn(name="user_id")}
    )
    private Set<User> administrators = new HashSet<>();

    @OrderColumn(name="image_order")
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="business_id")
    private List<Image> images = new ArrayList<>();

    /**
     * Gets the id
     * @return Business Id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets business name
     * @param name Business name
     */
    public void setName(String name) {
        if (name == null || name.isEmpty() || name.isBlank()) {
            throw new ValidationResponseException("The business name must not be empty");
        }
        if (name.length() > 100) {
            throw new ValidationResponseException("The business name must be 100 characters or fewer");
        }
        if (!name.matches(TEXT_REGEX)) {
            throw new ValidationResponseException("The business name can contain only letters, " +
                    "numbers, and the special characters !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");
        }
        this.name = name;
    }

    /**
     * Gets business name
     * @return Business name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets business description
     * @param description Business description
     */
    public void setDescription(String description) {
        if (description == null || description.isEmpty() ||  description.isBlank()) {
            description = "";
        }
        if (description.length() > 200) {
            throw new ValidationResponseException("The business description must be 200 characters" +
                    " or fewer");
        }
        if (!description.matches(TEXT_REGEX)) {
            throw new ValidationResponseException("The business description can contain only letters, " +
                    "numbers, and the special characters @ $ % & - _ , . : ;");
        }
        this.description = description;
    }

    /**
     * Gets Business description
     * @return Business description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets business address
     * @param address business address
     */
    public void setAddress(Location address) {
        if (address == null) {
            throw new ValidationResponseException("The business's address cannot be null");
        }
        this.address = address;
    }

    /**
     * Gets business address
     * @return business address
     */
    public Location getAddress() {
        return this.address;
    }

    /**
     * Sets business type
     * @param businessType business type
     */
    public void setBusinessType(BusinessType businessType) {
        if (businessType == null) {
            throw new ValidationResponseException("The business type must not be empty");
        }
        this.businessType = businessType;
    }

    /**
     * Gets business type
     * @return business type
     */
    public BusinessType getBusinessType() {
        return this.businessType;
    }

    /**
     * Gets business date created
     * @param createdAt date created
     */
    private void setCreated(Instant createdAt) {
        if (createdAt == null) {
            throw new ValidationResponseException("The date the business was created cannot be null");
        }
        if (this.created != null) {
            throw new ValidationResponseException("The date the business was created cannot be reset");
        }
        this.created = createdAt;
    }

    /**
     * Gets date created
     * @return date created
     */
    public Instant getCreated() {
        return this.created;
    }

    /**
     * Increments the business' points in response to selling a listing
     */
    public void incrementPoints() {
        this.points += POINTS_PER_SALE_LISTING;
    }

    /**
     * Gets business' points total
     * @return Business points
     */
    public int getPoints(){return this.points;}

    /**
     * Sets the business' points total
     * @param points Value to set points
     */
    public void setPoints(int points){this.points = points;}

    /**
     * Gets the business' current rank
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Sets primary owner of the business
     * If the requested user is less than 16 years of age, a 403 forbidden status is thrown.
     * @param owner Owner of business
     */
    public void setPrimaryOwner(User owner) {
        if (owner == null) {
            throw new ValidationResponseException("The business must have a primary owner");
        }

        //Get the current date as of now and find the difference in years between the current date and the age of the user.
        long age = java.time.temporal.ChronoUnit.YEARS.between(
            owner.getDob(), LocalDate.now());
        if (age < MINIMUM_AGE) {
            throw new InsufficientPermissionResponseException("User is not of minimum age required to create a business");
        }
        this.primaryOwner = owner;
    }

    /**
     * Gets primary owner of the business
     * @return Primary owner
     */
    public User getPrimaryOwner() {
        return this.primaryOwner;
    }

    /**
     * Gets the set of Users who are an admin of this business
     * @return Business admins
     */
    public Set<User> getAdministrators() {
        return this.administrators;
    }

    /**
     * Adds a new admin to the business
     * Throws an ResponseStatusException if the user is already an admin
     * @param newAdmin The user to make admininstrator
     */
    public void addAdmin(User newAdmin) {
        if (this.administrators.contains(newAdmin) || this.primaryOwner == newAdmin) {
            throw new ValidationResponseException("This user is already a registered admin of this business");
        } else {
            this.administrators.add(newAdmin);
        }
    }

    /**
     * Removes an admin from a business
     * Throws an ResponseStatusException if the user is not an admin of the business
     * @param oldAdmin the user to revoke administrator
     */
    public void removeAdmin(User oldAdmin) {
        if (!this.administrators.contains(oldAdmin)) {
            throw new ValidationResponseException("The given user is not an admin of this business");
        } else {
            this.administrators.remove(oldAdmin);
        }
    }

    /**
     * This method checks if the account associated with the current session has permission to act as this business (i.e.,
     * the user is either an admin of the business or a GAA). If the account does not have permission to act as this
     * business, a response status exception with status code 403 will be thrown.
     */
    public void checkSessionPermissions(HttpServletRequest request) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        HttpSession session = request.getSession(false);
        Long userId = (Long) session.getAttribute("accountId");
        Set<Long> adminIds = new HashSet<>();
        for (User user : getOwnerAndAdministrators()) {
            adminIds.add(user.getUserID());
        }
        if (!AuthenticationTokenManager.sessionIsAdmin(request) && !adminIds.contains(userId)) {
            throw new InsufficientPermissionResponseException("User does not have sufficient permissions to perform this action");
        }
    }
    /**
     * Check the account associated with the current session is either the primaryOener of the business
     * Or a system administrator
     * If the account does not have permission, 403 will be thrown
     * @param request 
     */
    public void checkSessionPermissionsOwner(HttpServletRequest request) {
        AuthenticationTokenManager.checkAuthenticationToken(request);
        HttpSession session = request.getSession(false);
        Long userId = (Long) session.getAttribute("accountId");

        if (!AuthenticationTokenManager.sessionIsAdmin(request) && !this.getPrimaryOwner().getUserID().equals(userId)) {
            throw new InsufficientPermissionResponseException("Only Primary Owner and System administrator can perform this action");
        }
    }

    /**
     * This method retrieves all Users who are owners or admins of this business.
     * @return A Set containing Users who are owners or admins of the business.
     */
    public Set<User> getOwnerAndAdministrators() {
        Set<User> ownerAdminSet = new HashSet<>();
        ownerAdminSet.addAll(administrators);
        ownerAdminSet.add(primaryOwner);
        return ownerAdminSet;
    }



    /**
     * Add the given product to the business's catalogue.
     * This function is only expected to be called from "Product.setBusiness"
     *
     * @param product The product to be added.
     */
    public void addToCatalogue(Product product) {
        if (product.getBusiness() != this) {
            throw new IllegalArgumentException("\"addToCatalogue\" is not being called from \"Product.setBusiness\"");
        }
        catalogue.add(product);
    }

    /**
     * Returns the business's product catalogue.
     * @return product catalogue of the business.
     */
    public List<Product> getCatalogue() {
        return this.catalogue;
    }

    /**
     * Returns the images associated with this business.
     * @return List of business images
     */
    @Override
    public List<Image> getImages() { return this.images; }

    /**
     * Replaces the existing list of images with a new list of images
     * @param images A list of images
     */
    public void setImages(List<Image> images) {
        this.images = images;
    }

    /**
     * Returns the ids of all the images associated with the business
     * @return the ids of all the images associated with the business
     */
    public List<Long> getIdsOfImages() {
        List<Long> imageIds = new ArrayList<Long>();
        for (Image image: this.images) {
            imageIds.add(image.getID());
        }
        return imageIds;
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Builder for Business
     */
    public static class Builder {
        private String name;
        private String description;
        private User primaryOwner;
        private Location address;
        private BusinessType businessType;

        /**
         * Sets the builders name. Required
         * @param name Name of the business
         * @return Builder with name set
         */
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        /**
         * Sets the builders description. Required
         * @param description Name of the business
         * @return Builder with description set
         */
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        /**
         * Sets the builders primary owner. Required
         * @param owner Name of the business
         * @return Builder with primary owner set
         */
        public Builder withPrimaryOwner(User owner) {
            this.primaryOwner = owner;
            return this;
        }
        /**
         * Sets the builders address. Required
         * @param address Name of the business
         * @return Builder with address set
         */
        public Builder withAddress(Location address) {
            this.address = address;
            return this;
        }
        /**
         * Sets the builders businessType. Required
         * @param businessType Name of the business
         * @return Builder with businessType set
         */
        public Builder withBusinessType(BusinessType businessType) {
            this.businessType = businessType;
            return this;
        }

        /**
         * Builds the business
         * @return The newly created Business
         */
        public Business build() {
            Business business = new Business();
            business.setName(this.name);
            business.setAddress(this.address);
            business.setBusinessType(this.businessType);
            business.setDescription(this.description);
            business.setCreated(Instant.now());
            business.setPrimaryOwner(this.primaryOwner);
            return business;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Business)) {
            return false;
        }
        Business business = (Business) o;
        return
                this.id.equals(business.getId()) &&
                this.name.equals(business.getName()) &&
                this.description.equals(business.getDescription()) &&
                ChronoUnit.SECONDS.between(this.created, business.getCreated()) == 0;
    }

    @Override
    public int hashCode() {
        return this.id.intValue();
    }

}
