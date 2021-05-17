package org.seng302.entities;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

@Entity
public class MarketplaceCard {
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


    /**
     * Gets the id (will be unique among marketplace cards)
     * @return card id
     */
    public Long getId() {
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
     * @return Card description
     */
    public String getDescription() {
        return description;
    }

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
        this.title = title;
    }

    /**
     * Sets and validates the card description
     * @param description New description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets and validates the card closing date and time
     * @param closes New closing date and time
     */
    public void setCloses(Instant closes) {
        this.closes = closes;
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
     * Determines whether one marketplace card is the same as another
     * @param o Object to test against
     * @return Whether these objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketplaceCard)) return false;
        MarketplaceCard that = (MarketplaceCard) o;
        return  Objects.equals(id, that.id) &&
                Objects.equals(creator, that.creator) &&
                section == that.section &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(created, that.created) &&
                Objects.equals(closes, that.closes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creator, section, title, description, created, closes);
    }

    /**
     * Valid marketplace card sections
     */
    public enum Section {
        FOR_SALE("ForSale"),
        WANTED("Wanted"),
        EXCHANGE("Exchange");

        private final String name;

        Section(String name) {
            this.name = name;
        }

        /**
         * Gets the name of the section.
         * Compatible with api
         * @return section name
         */
        public String getName() {
            return this.name;
        }
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
            for (Section possibleSection : Section.values()) {
                if (possibleSection.getName().equals(sectionName)) {
                    this.section = possibleSection;
                    return this;
                }
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid section name");
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
         * Builds the marketplace card
         * @return Newly created marketplace card
         */
        public MarketplaceCard build() {
            var card = new MarketplaceCard();
            card.creator = creator;
            card.setSection(section);
            card.setTitle(title);
            card.setDescription(description);
            card.created = Instant.now();
            if (closes == null) {
                card.setCloses(card.created.plus(2, ChronoUnit.WEEKS));
            } else {
                card.setCloses(closes);
            }
            return card;
        }
    }
}
