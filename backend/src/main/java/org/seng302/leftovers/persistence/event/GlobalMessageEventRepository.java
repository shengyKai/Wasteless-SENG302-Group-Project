package org.seng302.leftovers.persistence.event;

import org.seng302.leftovers.entities.event.GlobalMessageEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for persisting and accessing KeywordCreatedEvent data from the database.
 */
@Repository
public interface GlobalMessageEventRepository extends CrudRepository<GlobalMessageEvent, Long> {

}
