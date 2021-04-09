package org.seng302.Entities;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import javax.persistence.*;
import java.util.*;

@Entity
public class Business {

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
     * @param owner Owner of business
     */
    private void setPrimaryOwner(User owner) {
        if (owner == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The business must have a primary owner");
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

    public void addAdmin(User newAdmin) {
        if (this.administrators.contains(newAdmin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user is already a registered admin of this business");
        } else {
            this.administrators.add(newAdmin);
        }
    }

    /**
     * Construct a JSON object representing the business. The JSON object includes an array of JSON
     * representations of the users who are administrators of the business, and a JSON representation
     * of the business's address, as well as simple attributes for all the other properties of the
     * business.
     * @return A JSON representation of this business.
     */
    public JSONObject constructJson() {
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put("id", getId());
        attributeMap.put("name", name);
        attributeMap.put("description", description);
        attributeMap.put("administrators", constructAdminJsonArray());
        attributeMap.put("primaryAdministratorId", primaryOwner.getUserID());
        attributeMap.put("address", getAddress().constructFullJson());
        attributeMap.put("businessType", businessType);
        attributeMap.put("created", created.toString());
        return new JSONObject(attributeMap);
    }

    /**
     * This method gets the public JSON representation of each User who is an admin of this Business
     *  and adds it to a JSONArray.
     * @return A JSONArray containing JSON respresentations of all admins of this business.
     */
    private JSONArray constructAdminJsonArray() {
        JSONArray adminArray = new JSONArray();
        for (User admin : administrators) {
            adminArray.add(admin.constructPublicJson());
        }
        return adminArray;
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

}
