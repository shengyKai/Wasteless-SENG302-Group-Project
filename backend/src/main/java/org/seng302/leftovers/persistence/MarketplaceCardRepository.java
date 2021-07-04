package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Repository
public interface MarketplaceCardRepository extends CrudRepository<MarketplaceCard, Long> {
    /**
     * Finds all the marketplace cards created by a given user
     * @param user User that the cards belong to
     * @return List of created cards
     */
    List<MarketplaceCard> getAllByCreator(@Param("Creator") User user);

    /**
     * Finds all the marketplace cards with the given keyword
     * @param keyword Keyword to search for
     * @return List of cards with the keyword
     */
    List<MarketplaceCard> getAllByKeywords(@Param("keywords") Keyword keyword);

    /**
     * Finds all the marketplace cards that are in the given section
     * @param section Section to filter by
     * @return List of cards within that section
     */
    List<MarketplaceCard> getAllBySection(@Param("section") MarketplaceCard.Section section);

    /**
     * Fetches a marketplace card from the database for the given card id. This method will also check that
     * the card exists and will throw a 404 response exception if no card exists with the given id.
     * @param id Card id to filter by
     * @return Marketplace card with the given id
     */
    default MarketplaceCard getCard(Long id) {
        return findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No card exists with the given id"));
    }

    /**
     * Return all cards which have an closing data after the given cutoff data and which do not already have an associated
     * expiry event.
     * @param cutOff Cards with a closing date later than this date will be returned.
     * @return a list of all marketplace cards which need to have an expiry event sent.
     */
    @Query("SELECT c FROM MarketplaceCard c left join ExpiryEvent e ON e.expiringCard.id = c.id WHERE c.closes < :cutOff AND e is null")
    List<MarketplaceCard> getAllExpiringBefore(Instant cutOff);
}
