package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.KeywordCreatedEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository class for persisting and accessing KeywordCreatedEvent data from the database.
 */
@Repository
public interface CreateKeywordEventRepository extends CrudRepository<KeywordCreatedEvent, Long> {

    Optional<KeywordCreatedEvent> getByNewKeyword(Keyword keyword);

}
