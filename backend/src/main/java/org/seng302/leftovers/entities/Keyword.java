package org.seng302.leftovers.entities;

import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
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
     * Constructs a JSON representation of the keyword
     * @return JSON object containing keyword data
     */
    public JSONObject constructJSONObject() {
        JSONObject json = new JSONObject();
        json.appendField("id", this.getID());
        json.appendField("name", this.getName());
        json.appendField("created", this.getCreated().toString());
        return json;
    }

    /**
     * Formats keyword
     * @param name to be turned into keyword
     * @return formatted keyword name
     */
    private String formatName(String name) {
        if (name == null) return null;

        String[] words = name.split(" ");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            if (word.length() > 1) { // 2+ characters, normal
                formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
            } else if (word.length() == 1) { // 1 character, likely a vowel that doesn't need capitalisation
                formattedName.append(word).append(" ");
            } // else space, ignore
        }
        return formattedName.toString().strip();
    }

    /**
     * Formats, sets and validates the keyword name
     * @param name new keyword name
     */
    public void setName(String name) {
        name = formatName(name);
        if (name == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keyword name must be provided");
        }
        if (name.isEmpty() || name.length() > 25) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keyword name must be between 1-25 characters long");
        }
        if (!name.matches("^[ \\p{L}]*$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keyword name must only contain letters");
        }
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
