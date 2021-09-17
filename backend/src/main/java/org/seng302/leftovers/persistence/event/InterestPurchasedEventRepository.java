package org.seng302.leftovers.persistence.event;

import org.seng302.leftovers.entities.event.InterestPurchasedEvent;
import org.springframework.data.repository.CrudRepository;

public interface InterestPurchasedEventRepository extends CrudRepository<InterestPurchasedEvent, Long> {

}
