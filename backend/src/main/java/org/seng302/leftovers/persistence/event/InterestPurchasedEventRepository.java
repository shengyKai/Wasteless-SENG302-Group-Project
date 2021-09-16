package org.seng302.leftovers.persistence.event;

import org.seng302.leftovers.entities.event.InterestEvent;
import org.springframework.data.repository.CrudRepository;

public interface InterestPurchasedEventRepository extends CrudRepository<InterestEvent, Long> {

}
