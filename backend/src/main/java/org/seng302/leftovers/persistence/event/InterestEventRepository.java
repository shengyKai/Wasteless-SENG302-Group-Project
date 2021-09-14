package org.seng302.leftovers.persistence.event;

import org.seng302.leftovers.entities.SaleItem;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.InterestEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository class for persisting and accessing InterestEvent entities in the database.
 */
public interface InterestEventRepository extends CrudRepository<InterestEvent, Long> {
    /**
     * This method returns an optional containing the interest event associated with the given user and sale item, if it exists.
     * @param notifiedUser User that is notified by the event
     * @param saleItem SaleItem that is liked/unliked
     * @return A optional containing an interest event associated with the user+sale item, if there is one.
     */
    Optional<InterestEvent> findInterestEventByNotifiedUserAndSaleItem(User notifiedUser, SaleItem saleItem);
}
