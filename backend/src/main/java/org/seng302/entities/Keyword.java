package org.seng302.entities;

import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Entity
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private Instant created;

    @ManyToMany(mappedBy = "keywords")
    private List<MarketplaceCard> cards = new ArrayList<>();

    /**
     * Constructs a keyword with the given name
     * @param name name of the new keyword
     */
    public Keyword(String name) {
        created = Instant.now();
        setName(name);
    }

    /**
     * Gets the id (will be unique among keywords)
     * @return keyword id
     */
    public Long getID() {
        return id;
    }

    /**
     * Gets the name of this keyword
     * @return keyword name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the creation date and time for this keyword
     * @return creation date and time
     */
    public Instant getCreated() {
        return created;
    }

    /**
     * Sets and validates the keyword name
     * @param name new keyword name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Keyword{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", created=" + created +
                '}';
    }
}
