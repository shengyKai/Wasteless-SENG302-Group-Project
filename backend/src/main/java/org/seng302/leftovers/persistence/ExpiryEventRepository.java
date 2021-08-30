package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.event.ExpiryEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository class for persisting and accessing ExpiryEvent data from the database.
 */
@Repository
public interface ExpiryEventRepository  extends CrudRepository<ExpiryEvent, Long> {

    /**
     * This method returns an optional containing the expiry event associated with the given card, if it exists.
     * @param marketplaceCard A card which may have an associated expiry event.
     * @return A optional containing an expiry event associated with the card, if there is one.
     */
    Optional<ExpiryEvent> getByExpiringCard(MarketplaceCard marketplaceCard);
}
