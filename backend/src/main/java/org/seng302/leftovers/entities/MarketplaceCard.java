package org.seng302.leftovers.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.seng302.leftovers.exceptions.ValidationResponseException;

import javax.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
public class MarketplaceCard {
    private static final Duration DISPLAY_PERIOD = Duration.ofDays(14);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Section section;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Instant created;

    @Column(nullable = false)
    private Instant closes;

    @Column(name = "last_renewed", nullable = false)
    private Instant lastRenewed;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "card_keywords")
    private List<Keyword> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Conversation> conversations = new ArrayList<>();


    /**
     * Gets the id (will be unique among marketplace cards)
     * @return card id
     */
    public Long getID() {
        return id;
    }

    /**
     * Gets the creator of this card
     * @return creator user
     */
    public User getCreator() {
        return creator;
    }

    /**
     * Gets the section of this card
     * @return card section
     */
    public Section getSection() {
        return section;
    }

    /**
     * Gets the title of this card
     * @return card title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the moment this card was created
     * @return creation date and time
     */
    public Instant getCreated() { return created; }

    /**
     * Gets the moment this card will close
     * @return closing date and time
     */
    public Instant getCloses() { return closes; }

    /**
     * Get the last date on which this card was renewed. If the card has never been renewed, returns the creation date.
     * @return date of last renewal or creation if there have been no renewals.
     */
    public Instant getLastRenewed() {return lastRenewed;}

    /**
     * @return Card description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Cards keywords
     */
    public List<Keyword> getKeywords() {return this.keywords;}

    /**
     * Sets the card section
     * @param section New section
     */
    public void setSection(Section section) {
        this.section = section;
    }

    /**
     * Sets and validates the card title
     * @param title New title
     */
    public void setTitle(String title) {
        if (title == null) {
            throw new ValidationResponseException("Card title must be provided");
        }
        if (title.isEmpty() || title.length() > 50) {
            throw new ValidationResponseException("Card title must be between 1-50 characters long");
        }
        if (!title.matches("^[ \\d\\p{Punct}\\p{L}]*$")) {
            throw new ValidationResponseException("Card title must only contain letters, numbers, spaces and punctuation");
        }
        this.title = title;
    }

    /**
     * Sets and validates the card description
     * @param description New description
     */
    public void setDescription(String description) {
        if (description == null || description.isEmpty()) {
            this.description = null;
            return;
        }
        if (description.length() > 200) {
            throw new ValidationResponseException("Card description must not be longer than 200 characters");
        }
        if (!description.matches("^[\\p{Space}\\d\\p{Punct}\\p{L}]*$")) {
            throw new ValidationResponseException("Card description must only contain letters, numbers, whitespace and punctuation");
        }
        this.description = description;
    }

    /**
     * Sets and validates the card closing date and time
     * @param closes New closing date and time
     */
    public void setCloses(Instant closes) {
        if (closes == null) {
            throw new ValidationResponseException("Closing time cannot be null");
        }
        if (closes.isBefore(Instant.now())) {
            throw new ValidationResponseException("Closing time cannot be before creation");
        }
        this.closes = closes;
    }

    /**
     * Delays the closing date for this card by the display period (2 weeks). Additionally, sets the card's last renewal
     * date to the current datetime.
     */
    public void delayCloses() {
        if (Instant.now().isBefore(closes.minus(1, ChronoUnit.DAYS))) {
            throw new ValidationResponseException("Too early to extend closing date");
        }
        closes = closes.plus(DISPLAY_PERIOD);
        lastRenewed = Instant.now();
    }

    /**
     * Adds and validates a keyword to this Marketplace Card
     * @param keyword Keyword to add to card
     */
    public void addKeyword(Keyword keyword) {
        if (keyword == null) {
            throw new ValidationResponseException("Keyword cannot be null");
        }
        keywords.add(keyword);
    }

    /**
     * Validates and replaces all the keywords for this Markeplace Card
     * @param keywords New keywords for this card
     */
    public void setKeywords(List<Keyword> keywords) {
        if (keywords.stream().anyMatch(Objects::isNull)) {
            throw new ValidationResponseException("Keyword cannot be null");
        }
        this.keywords.clear();
        this.keywords.addAll(keywords);
    }

    /**
     * Removes the provided keyword from this marketplace card
     * @param keyword Keyword to remove to card
     */
    public void removeKeyword(Keyword keyword) {
        keywords.remove(keyword);
    }

    /**
     * Creates a string representation of the marketplace card
     * @return string representation
     */
    @Override
    public String toString() {
        return "MarketplaceCard{" +
                "id=" + id +
                ", creator=" + creator +
                ", section=" + section +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", created=" + created +
                ", closes=" + closes +
                '}';
    }

    /**
     * Valid marketplace card sections
     */
    public enum Section {
        @JsonProperty("ForSale")
        FOR_SALE,

        @JsonProperty("Wanted")
        WANTED,

        @JsonProperty("Exchange")
        EXCHANGE;

        private static final ObjectMapper mapper = new ObjectMapper();

        /**
         * Gets the name of the section.
         * Same as in api spec
         * @return section name
         */
        public String getName() {
            return mapper.convertValue(this, String.class);
        }
    }

    /**
     * Given a string, returns the matching section Enum
     * @param sectionName The section name to get
     * @return Matching section or ResponseStatusException if none
     */
    public static Section sectionFromString(String sectionName) {
        for (Section possibleSection : Section.values()) {
            if (possibleSection.getName().equals(sectionName)) {
                return possibleSection;
            }
        }
        throw new ValidationResponseException("Invalid section name");
    }

    /**
     * This class uses the builder pattern to construct an instance of the MarketplaceCard class
     */
    public static class Builder {
        private User creator;
        private Section section;
        private String title;
        private String description;
        private Instant closes;
        private List<Keyword> keywords = new ArrayList<>();

        /**
         * Sets the builder's creator.
         *
         * @param creator Creator for the marketplace card
         * @return Builder with the creator set
         */
        public Builder withCreator(User creator) {
            this.creator = creator;
            return this;
        }

        /**
         * Sets the builder's section.
         *
         * @param section Section for the marketplace card
         * @return Builder with the section set
         */
        public Builder withSection(Section section) {
            this.section = section;
            return this;
        }

        /**
         * Sets the builder's section.
         *
         * @param sectionName Name of the section for the marketplace card
         * @return Builder with the section set
         */
        public Builder withSection(String sectionName) {
            this.section = MarketplaceCard.sectionFromString(sectionName);
            return this;
        }

        /**
         * Sets the builder's title.
         *
         * @param title Title for the marketplace card
         * @return Builder with the title set
         */
        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the builder's description.
         *
         * @param description Description for the marketplace card
         * @return Builder with the description set
         */
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the builder's close date and time.
         * If no closing date is provided then a two week interval after the creation moment is used.
         *
         * @param closes Creator for the marketplace card
         * @return Builder with the closing date and time set
         */
        public Builder withCloses(Instant closes) {
            this.closes = closes;
            return this;
        }

        /**
         * Adds a single keyword to this builder
         * @param keyword keyword to add
         * @return Builder with keyword added
         */
        public Builder addKeyword(Keyword keyword) {
            keywords.add(keyword);
            return this;
        }

        /**
         * Adds all the keywords in the keyword collection to this builder
         * @param keywords keywords to add
         * @return Builder with keywords added
         */
        public Builder addKeywords(Collection<Keyword> keywords) {
            this.keywords.addAll(keywords);
            return this;
        }

        /**
         * Builds the marketplace card
         * @return Newly created marketplace card
         */
        public MarketplaceCard build() {
            var card = new MarketplaceCard();

            if (creator == null) {
                throw new ValidationResponseException("Card creator not provided");
            }
            card.creator = creator;
            card.setSection(section);
            card.setTitle(title);
            card.setDescription(description);
            card.created = Instant.now();
            card.lastRenewed = card.created;
            if (closes == null) {
                card.setCloses(card.created.plus(DISPLAY_PERIOD));
            } else {
                card.setCloses(closes);
            }
            card.setKeywords(keywords);
            return card;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketplaceCard that = (MarketplaceCard) o;
        return id.equals(that.id) && section == that.section &&
                title.equals(that.title) &&
                Objects.equals(description, that.description) &&
                ChronoUnit.SECONDS.between(this.created, that.created) == 0 &&
                ChronoUnit.SECONDS.between(this.closes, that.closes) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
