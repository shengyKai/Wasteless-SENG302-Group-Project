package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant created = Instant.now();

    @ManyToMany
    @JoinTable(name = "event_users")
    private Set<User> notifiedUsers = new HashSet<>();

    /**
     * Constructs a JSON representation of this event
     * Subclasses are expected to override this method and add their own attributes
     * @return JSON object containing event data
     */
    public JSONObject constructJSONObject() {
        JSONObject json = new JSONObject();
        json.appendField("id", this.getId());
        json.appendField("created", this.getCreated().toString());
        return json;
    }

    /**
     * Gets the public key for this event
     * @return Event public key
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the moment when this event was created
     * @return Event creation moment
     */
    public Instant getCreated() {
        return created;
    }

    /**
     * Adds a set of users to this event.
     * This method is only expected to be called from EventService, since it does not notify the users
     * @param users Users to add
     */
    public void addUsers(Set<User> users) {
        notifiedUsers.addAll(users);
    }
}
