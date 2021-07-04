package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.ExpiryEvent;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ExpiryEventRepository  extends CrudRepository<ExpiryEvent, Long> {

    /**
     * This method returns an optional containing the expiry event associated with the given card, if it exists.
     * @param marketplaceCard A card which may have an associated expiry event.
     * @return A optional containing an expiry event associated with the card, if there is one.
     */
    Optional<ExpiryEvent> getByExpiringCard(MarketplaceCard marketplaceCard);
}
