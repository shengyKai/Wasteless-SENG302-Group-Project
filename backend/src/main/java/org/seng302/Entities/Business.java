package org.seng302.Entities;

import org.seng302.Tools.AuthenticationTokenManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ser.impl.StringArraySerializer;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Entity
public class Business {

    //Minimum age to create a business
    private final int MinimumAge = 16;
    private static final List<String> businessTypes = new ArrayList<>(Arrays.asList("Accommodation and Food Services", "Retail Trade", "Charitable organisation", "Non-profit organisation"));

    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Location address;
    @Column(nullable = false)
    private String businessType;
    @Column
    private Date created;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User primaryOwner;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name="business_admins",
            joinColumns = {@JoinColumn(name="business_id")},
            inverseJoinColumns = {@JoinColumn(name="user_id")}
    )
    private Set<User> administrators = new HashSet<>();


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
        if (name == null || name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The business name must not be empty");
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
        if (description == null || description.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The business description must not be empty");
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
    public void setBusinessType(String businessType) {
        if (businessType == null || businessType.isEmpty() || !businessTypes.contains(businessType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The business type must not be empty and must be one of: " + businessTypes.toString());
        }
        this.businessType = businessType;
    }

    /**
     * Gets business type
     * @return business type
     */
    public String getBusinessType() {
        return this.businessType;
    }

    /**
     * Gets business date created
     * @param createdAt date created
     */
    private void setCreated(Date createdAt) {
        this.created = createdAt;
    }

    /**
     * Gets date created
     * @return date created
     */
    public Date getCreated() {
        return this.created;
    }

    /**
     * Sets primary owner of the business
     * If the requested user is less than 16 years of age, a 403 forbidden status is thrown.
     * @param owner Owner of business
     */
    private void setPrimaryOwner(User owner) {
        if (owner == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The business must have a primary owner");
        }

        //Get the current date as of now and find the difference in years between the current date and the age of the user.
        long age = java.time.temporal.ChronoUnit.YEARS.between(
            owner.getDob().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now());
        if (age < MinimumAge) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not of minimum age required to create a business");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user is already a registered admin of this business");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given user is not an admin of this business");
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
        if (!AuthenticationTokenManager.sessionCanSeePrivate(request, null) && !adminIds.contains(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not have sufficient permissions to perform this action");
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
     * Construct a JSON object representing the business. The JSON object includes an array of JSON
     * representations of the users who are administrators of the business, and a JSON representation
     * of the business's address, as well as simple attributes for all the other properties of the
     * business. If fullAdminDetails is true, the JSON will include a full JSON representation for each
     * admin of the business. If fullAdminDetails is false, the administrators field will be excluded, to
     * avoid issues when nesting this json within the businessesAdministered field of the user json.
     * @param fullAdminDetails True if administrators should be included in JSON
     * @return A JSON representation of this business.
     */
    public JSONObject constructJson(boolean fullAdminDetails) {
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put("id", getId());
        attributeMap.put("name", name);
        attributeMap.put("description", description);
        if (fullAdminDetails) {
            attributeMap.put("administrators", constructAdminJsonArray());
        }
        attributeMap.put("primaryAdministratorId", primaryOwner.getUserID());
        attributeMap.put("address", getAddress().constructFullJson());
        attributeMap.put("businessType", businessType);
        attributeMap.put("created", created.toString());
        return new JSONObject(attributeMap);
    }

    /**
     * Override the constructJson method so that by default it does not includethe administrators.
     * @return A JSON representation of the business without details of its administrators.
     */
    public JSONObject constructJson() {
        return constructJson(false);
    }

    /**
     * This method gets the public JSON representation of each User who is an admin of this Business
     *  and adds it to a JSONArray. The JSONs in the array are ordered by the id number of the user
     *  to ensure consistency between subsequent requests.
     * @return A JSONArray containing JSON respresentations of all admins of this business.
     */
    private JSONArray constructAdminJsonArray() {
        JSONArray adminJsons = new JSONArray();
        List<User> admins = new ArrayList<>();
        admins.addAll(getOwnerAndAdministrators());
        Collections.sort(admins, (User user1, User user2) -> 
            user1.getUserID().compareTo(user2.getUserID()));
        for (User admin : admins) {
            adminJsons.add(admin.constructPublicJson());
        }
        return adminJsons;
    }


    /**
     * Builder for Business
     */
    public static class Builder {
        private String name;
        private String description;
        private User primaryOwner;
        private Location address;
        private String businessType;

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
        public Builder withBusinessType(String businessType) {
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
            business.setCreated(new Date());
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
                this.created.equals(business.getCreated());
    }

    @Override
    public int hashCode() {
        return this.id.intValue();
    }

}
